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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtenteRepository repo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwt = new JwtService("test-secret-test-secret-test-secret-0123456789", 60);
    private LoginThrottle throttle;
    private AuthService service;
    private Utente mario;

    @BeforeEach
    void setUp() {
        throttle = new LoginThrottle();
        service = new AuthService(repo, encoder, jwt, throttle);
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
}
