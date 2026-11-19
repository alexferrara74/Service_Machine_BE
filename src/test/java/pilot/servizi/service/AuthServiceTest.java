package pilot.servizi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import pilot.servizi.dto.request.CambioPasswordRequest;
import pilot.servizi.dto.request.LoginRequest;
import pilot.servizi.dto.response.LoginResponse;
import pilot.servizi.entity.RegistroAttivita.Azione;
import pilot.servizi.entity.RegistroAttivita.Esito;
import pilot.servizi.entity.Utente;
import pilot.servizi.entity.Utente.Ruolo;
import pilot.servizi.repository.UtenteRepository;
import pilot.servizi.security.JwtService;
import pilot.servizi.security.LoginThrottle;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtenteRepository repo;
    @Mock
    private RegistroService registro;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwt = new JwtService("test-secret-test-secret-test-secret-0123456789", 60);
    private LoginThrottle throttle;
    private AuthService service;
    private Utente mario;

    @BeforeEach
    void setUp() {
        throttle = new LoginThrottle();
        service = new AuthService(repo, encoder, jwt, throttle, registro);
        mario = Utente.builder().id(1L).username("mario").passwordHash(encoder.encode("password-corretta")).ruolo(Ruolo.VIEWER)
                .attivo(true).passwordModificataIl(LocalDateTime.now().minusDays(1)).creatoIl(LocalDateTime.now().minusDays(1)).build();
        when(repo.findByUsernameIgnoreCase("mario")).thenReturn(Optional.of(mario));
    }

    @Test
    void logsInWithTheRightPasswordAndRecordsTheAccess() {
        LoginResponse r = service.login(new LoginRequest("mario", "password-corretta"));

        assertNotNull(r.token());
        assertEquals(Ruolo.VIEWER, r.ruolo());
        assertNotNull(mario.getUltimoAccesso());
    }

    @Test
    void wrongPasswordUnknownUserAndDisabledUserGetTheSameError() {
        when(repo.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());
        BadCredentialsException sbagliata = assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("mario", "password-sbagliata")));
        BadCredentialsException inesistente = assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("ghost", "password-corretta")));
        mario.setAttivo(false);
        BadCredentialsException disattivato = assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("mario", "password-corretta")));

        assertEquals(sbagliata.getMessage(), inesistente.getMessage());
        assertEquals(sbagliata.getMessage(), disattivato.getMessage());
    }

    @Test
    void blocksTheAccountAfterTooManyFailuresEvenWithTheRightPassword() {
        for (int i = 0; i < LoginThrottle.MAX_TENTATIVI; i++) {
            assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("mario", "password-sbagliata")));
        }

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.login(new LoginRequest("mario", "password-corretta")));
        assertEquals(429, e.getStatusCode().value());
    }

    @Test
    void aSuccessfulLoginResetsTheFailureCount() {
        for (int i = 0; i < LoginThrottle.MAX_TENTATIVI - 1; i++) {
            assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("mario", "password-sbagliata")));
        }
        service.login(new LoginRequest("mario", "password-corretta"));

        assertFalse(throttle.bloccato("mario"));
        assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("mario", "password-sbagliata")));
        assertFalse(throttle.bloccato("mario"));
    }

    @Test
    void changesThePasswordOnlyWithTheCurrentOne() {
        assertThrows(IllegalArgumentException.class, () -> service.cambiaPassword("mario", new CambioPasswordRequest("sbagliata-sbagliata", "nuova-password-1")));
        assertThrows(IllegalArgumentException.class, () -> service.cambiaPassword("mario", new CambioPasswordRequest("password-corretta", "corta")));

        LocalDateTime prima = mario.getPasswordModificataIl();
        service.cambiaPassword("mario", new CambioPasswordRequest("password-corretta", "nuova-password-1"));

        assertTrue(encoder.matches("nuova-password-1", mario.getPasswordHash()));
        assertTrue(mario.getPasswordModificataIl().isAfter(prima));
    }

    @Test
    void recordsSuccessfulFailedAndBlockedLogins() {
        service.login(new LoginRequest("mario", "password-corretta"));
        verify(registro).registraAccesso(Azione.LOGIN, "mario", "VIEWER", Esito.OK, null);

        assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("mario", "password-sbagliata")));
        verify(registro).registraAccesso(eq(Azione.LOGIN_FALLITO), eq("mario"), eq(null), eq(Esito.NEGATO), any());

        for (int i = 0; i < LoginThrottle.MAX_TENTATIVI; i++) {
            try { service.login(new LoginRequest("mario", "password-sbagliata")); } catch (RuntimeException ignored) { }
        }
        assertThrows(ResponseStatusException.class, () -> service.login(new LoginRequest("mario", "password-corretta")));
        verify(registro, org.mockito.Mockito.atLeastOnce()).registraAccesso(eq(Azione.LOGIN_BLOCCATO), eq("mario"), eq(null), eq(Esito.RIFIUTATO), any());
    }

    @Test
    void neverPutsThePasswordInTheRegistry() {
        assertThrows(BadCredentialsException.class, () -> service.login(new LoginRequest("mario", "password-sbagliata")));

        org.mockito.ArgumentCaptor<String> dettaglio = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(registro).registraAccesso(any(), any(), any(), any(), dettaglio.capture());
        assertFalse(dettaglio.getValue().contains("password-sbagliata"));
        verify(registro, never()).registraRichiesta(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt(), any());
    }
}
