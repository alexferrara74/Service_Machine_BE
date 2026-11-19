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
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AppuntamentoAssistenzaRepository;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
}
