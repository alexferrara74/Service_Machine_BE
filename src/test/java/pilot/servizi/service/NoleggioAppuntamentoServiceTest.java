package pilot.servizi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import pilot.servizi.dto.request.AppuntamentoRequest;
import pilot.servizi.dto.response.NoleggioManutenzioneResponse;
import pilot.servizi.entity.NoleggioAppuntamento;
import pilot.servizi.entity.NoleggioAppuntamento.Stato;
import pilot.servizi.entity.NoleggioAppuntamentoFoto;
import pilot.servizi.entity.NoleggioManutenzione;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.NoleggioAppuntamentoFotoRepository;
import pilot.servizi.repository.NoleggioAppuntamentoRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoleggioAppuntamentoServiceTest {

    @Mock private NoleggioAppuntamentoRepository repo;
    @Mock private NoleggioAppuntamentoFotoRepository fotoRepo;
    @Mock private StoriaNoleggiRepository noleggiRepo;
    @Mock private ClientiRepository clientiRepo;
    @Mock private NoleggioManutenzioneService manutenzioneService;

    private NoleggioAppuntamentoService service;
    private NoleggioAppuntamento aperto;

    @BeforeEach
    void setUp() {
        service = new NoleggioAppuntamentoService(repo, fotoRepo, noleggiRepo, clientiRepo, manutenzioneService);
        aperto = NoleggioAppuntamento.builder().id(5L).docId(42).stato(Stato.PROGRAMMATO)
                .tipo(NoleggioManutenzione.Tipo.ORDINARIA).dataPrevista(LocalDateTime.now()).build();
        lenient().when(noleggiRepo.findById(42)).thenReturn(Optional.of(StoriaNoleggi.builder().docId(42).matricola("M1").build()));
        lenient().when(repo.findByIdAndDocId(5L, 42)).thenReturn(Optional.of(aperto));
        lenient().when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(fotoRepo.findByAppuntamentoIdOrderByIdAsc(anyLong())).thenReturn(List.of());
        NoleggioManutenzione evento = NoleggioManutenzione.builder().id(99L).eseguitaIl(LocalDateTime.now()).build();
        lenient().when(manutenzioneService.registraDaAppuntamento(anyInt(), any(), any(), any(), anyInt(), any())).thenReturn(evento);
    }

    private static long anyLong() { return org.mockito.ArgumentMatchers.anyLong(); }

    private AppuntamentoRequest chiusura(String tipo, String esito, String resoconto, Integer proroga) {
        return new AppuntamentoRequest(LocalDateTime.now(), tipo, esito, resoconto, proroga);
    }

    @Test
    void positiveOutcomeRecordsTheOperatorChosenTypeAndTheExtension() {
        var out = service.completa(42, 5L, chiusura("STRAORDINARIA", "POSITIVO", "Tutto regolare", 20));

        assertEquals("COMPLETATO", out.stato());
        assertEquals(20, out.giorniProroga());
        verify(manutenzioneService).registraDaAppuntamento(42, NoleggioManutenzione.Tipo.STRAORDINARIA,
                "Tutto regolare", "POSITIVO", 20, 5L);
        assertEquals(99L, aperto.getManutenzioneId());
    }

    @Test
    void positiveOutcomeWithoutDaysProposesHalfTheInterval() {
        NoleggioManutenzioneResponse stato = mock(NoleggioManutenzioneResponse.class);
        when(stato.intervalloManutenzioneGiorni()).thenReturn(60);
        when(manutenzioneService.getStato(42)).thenReturn(stato);

        service.completa(42, 5L, chiusura("ORDINARIA", "POSITIVO", "ok", null));

        verify(manutenzioneService).registraDaAppuntamento(42, NoleggioManutenzione.Tipo.ORDINARIA, "ok", "POSITIVO", 30, 5L);
    }

    @Test
    void negativeOutcomeNeverExtends() {
        service.completa(42, 5L, chiusura("ORDINARIA", "NEGATIVO", "guasto", 40));

        verify(manutenzioneService).registraDaAppuntamento(42, NoleggioManutenzione.Tipo.ORDINARIA, "guasto", "NEGATIVO", 0, 5L);
    }

    @Test
    void closingRequiresOutcomeAndReport() {
        assertThrows(IllegalArgumentException.class, () -> service.completa(42, 5L, chiusura("ORDINARIA", null, "x", null)));
        aperto.setEsito(null);
        assertThrows(IllegalArgumentException.class, () -> service.completa(42, 5L, chiusura("ORDINARIA", "POSITIVO", "  ", null)));
        verify(manutenzioneService, never()).registraDaAppuntamento(anyInt(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void rejectsAnInvalidType() {
        assertThrows(IllegalArgumentException.class, () -> service.update(42, 5L, chiusura("BOH", null, null, null)));
    }

    @Test
    void aClosedAppointmentCannotBeChangedOrDeleted() {
        aperto.setStato(Stato.COMPLETATO);
        assertThrows(DataIntegrityViolationException.class, () -> service.update(42, 5L, chiusura("ORDINARIA", null, "x", null)));
        assertThrows(DataIntegrityViolationException.class, () -> service.delete(42, 5L));
    }

    @Test
    void acceptsRealImagesAndRefusesOtherFiles() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
        when(fotoRepo.save(any())).thenAnswer(i -> { NoleggioAppuntamentoFoto f = i.getArgument(0); f.setId(1L); return f; });

        var foto = service.aggiungiFoto(42, 5L, new MockMultipartFile("file", "IMG 01.jpg", "image/jpeg", jpeg));
        assertEquals("IMG_01.jpg", foto.nomeFile());

        byte[] exe = "MZ not an image".getBytes();
        assertThrows(IllegalArgumentException.class,
                () -> service.aggiungiFoto(42, 5L, new MockMultipartFile("file", "x.jpg", "image/jpeg", exe)));
        verify(fotoRepo, times(1)).save(any());
    }
}
