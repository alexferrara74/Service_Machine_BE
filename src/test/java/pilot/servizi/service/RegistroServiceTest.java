package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pilot.servizi.entity.RegistroAttivita;
import pilot.servizi.entity.RegistroAttivita.Azione;
import pilot.servizi.entity.RegistroAttivita.Esito;
import pilot.servizi.repository.RegistroAttivitaRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroServiceTest {

    @Mock
    private RegistroAttivitaRepository repo;

    @InjectMocks
    private RegistroService service;

    private RegistroAttivita salvata() {
        ArgumentCaptor<RegistroAttivita> c = ArgumentCaptor.forClass(RegistroAttivita.class);
        verify(repo).save(c.capture());
        return c.getValue();
    }

    @Test
    void classifiesRequestsByMethod() {
        assertEquals(Azione.CREA, RegistroService.azione("POST", "/api/clienti"));
        assertEquals(Azione.MODIFICA, RegistroService.azione("PUT", "/api/clienti/1"));
        assertEquals(Azione.MODIFICA, RegistroService.azione("PATCH", "/api/appuntamenti/1/stato"));
        assertEquals(Azione.ELIMINA, RegistroService.azione("DELETE", "/api/clienti/1"));
        assertEquals(Azione.CAMBIO_PASSWORD, RegistroService.azione("POST", "/api/auth/password"));
        assertEquals(Azione.ESPORTA, RegistroService.azione("GET", "/api/appuntamenti/export"));
        assertNull(RegistroService.azione("GET", "/api/clienti"));
    }

    @Test
    void mapsStatusCodesToOutcomes() {
        assertEquals(Esito.OK, RegistroService.esito(201));
        assertEquals(Esito.OK, RegistroService.esito(204));
        assertEquals(Esito.NEGATO, RegistroService.esito(403));
        assertEquals(Esito.NEGATO, RegistroService.esito(401));
        assertEquals(Esito.RIFIUTATO, RegistroService.esito(400));
        assertEquals(Esito.RIFIUTATO, RegistroService.esito(409));
        assertEquals(Esito.ERRORE, RegistroService.esito(500));
    }

    @Test
    void splitsThePathIntoResourceIdAndDetail() {
        assertEquals(new RegistroService.Percorso("clienti", "7", null), RegistroService.analizza("/api/clienti/7"));
        assertEquals(new RegistroService.Percorso("storia/noleggi", "12", "manutenzione/configurazione"),
                RegistroService.analizza("/api/storia/noleggi/12/manutenzione/configurazione"));
        assertEquals(new RegistroService.Percorso("clienti", null, null), RegistroService.analizza("/api/clienti"));
    }

    @Test
    void storesAWriteRequestWithWhoWhatAndOutcome() {
        service.registraRichiesta("boss", "ADMIN", "DELETE", "/api/clienti/7", 409, "10.0.0.1");

        RegistroAttivita r = salvata();
        assertEquals("boss", r.getUsername());
        assertEquals("ELIMINA", r.getAzione());
        assertEquals("clienti", r.getRisorsa());
        assertEquals("7", r.getRisorsaId());
        assertEquals("RIFIUTATO", r.getEsito());
        assertEquals(409, r.getCodiceHttp());
        assertEquals("10.0.0.1", r.getIp());
    }

    @Test
    void ignoresPlainReads() {
        service.registraRichiesta("boss", "ADMIN", "GET", "/api/clienti", 200, "10.0.0.1");

        verify(repo, never()).save(any());
    }

    @Test
    void doesNotKeepAFailedLoginNameThatLooksLikeAPassword() {
        service.registraAccesso(Azione.LOGIN_FALLITO, "mario", null, Esito.NEGATO, "Credenziali non valide");
        assertEquals("mario", salvata().getUsername());
    }

    @Test
    void dropsAFailedLoginUsernameThatDoesNotLookLikeAUsername() {
        service.registraAccesso(Azione.LOGIN_FALLITO, "P@ssw0rd segreta!", null, Esito.NEGATO, "Credenziali non valide");

        assertNull(salvata().getUsername());
    }

    @Test
    void aFailureWritingTheRegistryNeverBreaksTheOperation() {
        when(repo.save(any())).thenThrow(new IllegalStateException("db giù"));

        service.registraRichiesta("boss", "ADMIN", "POST", "/api/clienti", 201, null);
        service.registraAccesso(Azione.LOGIN, "boss", "ADMIN", Esito.OK, null);
    }

    @Test
    void validatesPaginationFiltersAndRange() {
        assertThrows(IllegalArgumentException.class, () -> service.cerca(null, null, null, null, null, -1, 50));
        assertThrows(IllegalArgumentException.class, () -> service.cerca(null, null, null, null, null, 0, 500));
        assertThrows(IllegalArgumentException.class, () -> service.cerca(null, "BOH", null, null, null, 0, 50));
        LocalDateTime t = LocalDateTime.of(2026, 11, 19, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> service.cerca(null, null, null, t, t, 0, 50));
    }

    @Test
    void authEventsAreRegisteredInTheirOwnTransaction() throws Exception {
        // REQUIRES_NEW: il login viene annullato dall'eccezione, il registro dei tentativi falliti no.
        var m = RegistroService.class.getMethod("registraAccesso", Azione.class, String.class, String.class, Esito.class, String.class);
        var tx = m.getAnnotation(org.springframework.transaction.annotation.Transactional.class);
        assertEquals(org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, tx.propagation());
    }
}
