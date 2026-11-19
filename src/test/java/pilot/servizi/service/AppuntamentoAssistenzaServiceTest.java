package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pilot.servizi.dto.request.AppuntamentoRequest;
import pilot.servizi.dto.response.AppuntamentoResponse;
import pilot.servizi.entity.AppuntamentoAssistenza;
import pilot.servizi.entity.AppuntamentoAssistenza.Stato;
import pilot.servizi.entity.AppuntamentoAssistenzaFoto;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.Interventi;
import pilot.servizi.entity.StoriaNoleggi;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AppuntamentoAssistenzaFotoRepository;
import pilot.servizi.repository.AppuntamentoAssistenzaRepository;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.InterventiRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppuntamentoAssistenzaServiceTest {

    private static final LocalDateTime INIZIO = LocalDateTime.of(2026, 11, 20, 9, 0);
    private static final LocalDateTime FINE = LocalDateTime.of(2026, 11, 20, 10, 0);

    @Mock
    private AppuntamentoAssistenzaRepository repo;
    @Mock
    private StoriaNoleggiRepository noleggiRepo;
    @Mock
    private ClientiRepository clientiRepo;
    @Mock
    private AppuntamentoAssistenzaFotoRepository fotoRepo;
    @Mock
    private InterventiRepository interventiRepo;

    @InjectMocks
    private AppuntamentoAssistenzaService service;

    private AppuntamentoRequest request(Integer docId, String clienteNome, LocalDateTime inizio, LocalDateTime fine) {
        return new AppuntamentoRequest(docId, clienteNome, "333", "Via Roma 1", null, "Revisione", null, Stato.DA_FARE, inizio, fine, null);
    }

    @Test
    void createsAnAppointmentOnARegisteredRental() {
        StoriaNoleggi noleggio = StoriaNoleggi.builder().docId(5).clienteId(9L).matricola("M-1").descrizione("Idropulitrice").build();
        Clienti cliente = Clienti.builder().id(9L).ragioneSociale("Rossi Srl").telefono("011").build();
        when(noleggiRepo.existsById(5)).thenReturn(true);
        when(repo.save(any())).thenAnswer(i -> { AppuntamentoAssistenza e = i.getArgument(0); e.setId(1L); return e; });
        when(noleggiRepo.findAllById(List.of(5))).thenReturn(List.of(noleggio));
        when(clientiRepo.findAllById(List.of(9L))).thenReturn(List.of(cliente));

        // I dati manuali inviati insieme a un noleggio censito non vengono salvati: fa fede l'anagrafica.
        AppuntamentoResponse r = service.create(request(5, "Ignorato", INIZIO, FINE));

        ArgumentCaptor<AppuntamentoAssistenza> saved = ArgumentCaptor.forClass(AppuntamentoAssistenza.class);
        verify(repo).save(saved.capture());
        assertNull(saved.getValue().getClienteNome());
        assertTrue(r.censito());
        assertEquals("Rossi Srl", r.cliente());
        assertEquals("011", r.clienteTelefono());
        assertEquals("M-1", r.matricola());
        assertEquals("Idropulitrice", r.apparecchio());
    }

    @Test
    void createsAnAppointmentForAnUnregisteredCustomer() {
        when(repo.save(any())).thenAnswer(i -> { AppuntamentoAssistenza e = i.getArgument(0); e.setId(2L); return e; });

        AppuntamentoResponse r = service.create(request(null, "  Bianchi  ", INIZIO, FINE));

        assertFalse(r.censito());
        assertEquals("Bianchi", r.cliente());
        assertEquals("333", r.clienteTelefono());
        assertNull(r.docId());
    }

    @Test
    void requiresACustomerNameWhenTheRentalIsNotRegistered() {
        assertThrows(IllegalArgumentException.class, () -> service.create(request(null, "  ", INIZIO, FINE)));
        verify(repo, never()).save(any());
    }

    @Test
    void rejectsAnUnknownRental() {
        when(noleggiRepo.existsById(99)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request(99, null, INIZIO, FINE)));
        verify(repo, never()).save(any());
    }

    @Test
    void rejectsAnEndNotAfterTheStart() {
        assertThrows(IllegalArgumentException.class, () -> service.create(request(null, "Bianchi", FINE, INIZIO)));
        assertThrows(IllegalArgumentException.class, () -> service.create(request(null, "Bianchi", INIZIO, INIZIO)));
    }

    @Test
    void changesOnlyTheStatus() {
        AppuntamentoAssistenza e = AppuntamentoAssistenza.builder().id(3L).titolo("T").clienteNome("Bianchi")
                .stato(Stato.DA_CONFERMARE).inizio(INIZIO).fine(FINE).build();
        when(repo.findById(3L)).thenReturn(Optional.of(e));
        when(repo.save(e)).thenReturn(e);

        AppuntamentoResponse r = service.updateStato(3L, Stato.ANNULLATO);

        assertEquals(Stato.ANNULLATO, r.stato());
        assertEquals("T", r.titolo());
    }

    @Test
    void reportsAMissingAppointmentOnStatusChangeAndDelete() {
        when(repo.findById(8L)).thenReturn(Optional.empty());
        when(repo.existsById(8L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.updateStato(8L, Stato.FATTO));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(8L));
    }

    @Test
    void rejectsAnInvertedCalendarRange() {
        assertThrows(IllegalArgumentException.class, () -> service.getByIntervallo(FINE, INIZIO));
        assertThrows(IllegalArgumentException.class, () -> service.getByIntervallo(null, FINE));
    }

    // ---- chiusura: l'appuntamento fatto diventa un intervento effettuato ----------------------

    private AppuntamentoAssistenza appuntamentoSuNoleggio(Stato stato) {
        return AppuntamentoAssistenza.builder().id(7L).docId(5).titolo("Sostituzione guarnizione").note("Cambiata guarnizione")
                .stato(stato).inizio(INIZIO).fine(FINE).build();
    }

    @Test
    void closingAnAppointmentOnARentalCreatesACompletedIntervention() {
        AppuntamentoAssistenza e = appuntamentoSuNoleggio(Stato.DA_FARE);
        when(repo.findById(7L)).thenReturn(Optional.of(e));
        when(noleggiRepo.findById(5)).thenReturn(Optional.of(StoriaNoleggi.builder().docId(5).clienteId(9L).matricola(" M-1 ").build()));
        when(interventiRepo.save(any())).thenAnswer(i -> { Interventi x = i.getArgument(0); x.setInterventoId(70L); return x; });
        when(repo.save(e)).thenReturn(e);

        service.updateStato(7L, Stato.FATTO);

        ArgumentCaptor<Interventi> saved = ArgumentCaptor.forClass(Interventi.class);
        verify(interventiRepo).save(saved.capture());
        Interventi i = saved.getValue();
        assertEquals(9L, i.getClienteId());
        assertEquals("M-1", i.getMatricolaAttrezzatura());
        assertEquals("Sostituzione guarnizione", i.getTitolo());
        assertEquals("Cambiata guarnizione", i.getSoluzioneApplicata());
        assertEquals("COMPLETATO", i.getStato());
        assertEquals(INIZIO, i.getDataApertura());
        assertNotNull(i.getDataChiusura());
        assertFalse(i.getDataChiusura().isBefore(i.getDataApertura()));
        assertEquals(70L, e.getInterventoId());
    }

    @Test
    void closingAnAppointmentForAnUnregisteredCustomerCreatesNoIntervention() {
        AppuntamentoAssistenza e = AppuntamentoAssistenza.builder().id(8L).clienteNome("Bianchi").titolo("T")
                .stato(Stato.DA_FARE).inizio(INIZIO).fine(FINE).build();
        when(repo.findById(8L)).thenReturn(Optional.of(e));
        when(repo.save(e)).thenReturn(e);

        service.updateStato(8L, Stato.FATTO);

        verify(interventiRepo, never()).save(any());
    }

    @Test
    void closingTwiceKeepsASingleInterventionAndReopeningPutsItBackInProgress() {
        AppuntamentoAssistenza e = appuntamentoSuNoleggio(Stato.FATTO);
        e.setInterventoId(70L);
        Interventi i = new Interventi();
        i.setInterventoId(70L);
        i.setDataApertura(INIZIO);
        i.setStato("COMPLETATO");
        i.setDataChiusura(FINE);
        when(repo.findById(7L)).thenReturn(Optional.of(e));
        when(repo.save(e)).thenReturn(e);
        when(interventiRepo.findById(70L)).thenReturn(Optional.of(i));
        when(interventiRepo.save(i)).thenReturn(i);

        service.updateStato(7L, Stato.DA_FARE);
        assertEquals("IN_CORSO", i.getStato());
        assertNull(i.getDataChiusura());

        service.updateStato(7L, Stato.FATTO);
        assertEquals("COMPLETATO", i.getStato());
        assertNotNull(i.getDataChiusura());
        verify(interventiRepo, times(2)).save(i); // sempre lo stesso intervento: nessun duplicato
        assertEquals(70L, e.getInterventoId());
    }

    // ---- foto -----------------------------------------------------------------------------

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0, 0};

    @Test
    void attachesAPhotoAndStoresItsRealTypeNotTheFileName() {
        when(repo.findById(7L)).thenReturn(Optional.of(appuntamentoSuNoleggio(Stato.DA_FARE)));
        when(fotoRepo.countByAppuntamentoId(7L)).thenReturn(1L);
        when(fotoRepo.save(any())).thenAnswer(i -> { AppuntamentoAssistenzaFoto f = i.getArgument(0); f.setId(11L); return f; });

        AppuntamentoResponse.Foto r = service.aggiungiFoto(7L, new MockMultipartFile("file", "../tablet 01.jpg", "image/jpeg", PNG));

        ArgumentCaptor<AppuntamentoAssistenzaFoto> saved = ArgumentCaptor.forClass(AppuntamentoAssistenzaFoto.class);
        verify(fotoRepo).save(saved.capture());
        assertEquals("image/png", saved.getValue().getContentType());
        assertEquals("tablet_01.png", saved.getValue().getNomeFile());
        assertEquals(7L, saved.getValue().getAppuntamentoId());
        assertEquals(11L, r.id());
    }

    @Test
    void rejectsAFileThatIsNotAnImageAndAnEmptyOne() {
        when(repo.findById(7L)).thenReturn(Optional.of(appuntamentoSuNoleggio(Stato.DA_FARE)));
        when(fotoRepo.countByAppuntamentoId(7L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> service.aggiungiFoto(7L, new MockMultipartFile("file", "x.jpg", "image/jpeg", "<html>non sono una foto</html>".getBytes())));
        assertThrows(IllegalArgumentException.class, () -> service.aggiungiFoto(7L, new MockMultipartFile("file", "x.jpg", "image/jpeg", new byte[0])));
        verify(fotoRepo, never()).save(any());
    }

    @Test
    void refusesPhotosOverTheLimitAndOnCancelledAppointments() {
        when(repo.findById(7L)).thenReturn(Optional.of(appuntamentoSuNoleggio(Stato.DA_FARE)));
        when(fotoRepo.countByAppuntamentoId(7L)).thenReturn(20L);
        assertThrows(DataIntegrityViolationException.class, () -> service.aggiungiFoto(7L, new MockMultipartFile("file", "a.png", "image/png", PNG)));

        when(repo.findById(8L)).thenReturn(Optional.of(appuntamentoSuNoleggio(Stato.ANNULLATO)));
        assertThrows(DataIntegrityViolationException.class, () -> service.aggiungiFoto(8L, new MockMultipartFile("file", "a.png", "image/png", PNG)));
        verify(fotoRepo, never()).save(any());
    }

    @Test
    void deletingAnAppointmentDeletesItsPhotos() {
        when(repo.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(fotoRepo).deleteByAppuntamentoId(7L);
        verify(repo).deleteById(7L);
    }
}
