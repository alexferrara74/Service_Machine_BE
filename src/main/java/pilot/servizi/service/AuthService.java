package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pilot.servizi.dto.request.CambioPasswordRequest;
import pilot.servizi.dto.request.LoginRequest;
import pilot.servizi.dto.response.LoginResponse;
import pilot.servizi.dto.response.UtenteResponse;
import pilot.servizi.entity.Utente;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.UtenteRepository;
import pilot.servizi.security.JwtService;
import pilot.servizi.security.LoginThrottle;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    /** Hash di una password qualunque: confrontarlo anche per gli username inesistenti evita di svelarli dai tempi di risposta. */
    private static final String HASH_FITTIZIO = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5BUvOOgIzQkYsbVOvrjh6Qi2PpMdG";

    private final UtenteRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LoginThrottle throttle;

    @Transactional
    public LoginResponse login(LoginRequest dto) {
        String username = dto.username().trim();
        if (throttle.bloccato(username)) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "login", "Account temporaneamente bloccato: " + username);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, ErrorConstants.AUTH_TROPPI_TENTATIVI);
        }
        Utente utente = repo.findByUsernameIgnoreCase(username).orElse(null);
        boolean passwordOk = encoder.matches(dto.password(), utente != null ? utente.getPasswordHash() : HASH_FITTIZIO);
        // Stessa risposta per username inesistente, password errata o utente disattivato.
        if (utente == null || !passwordOk || !utente.isAttivo()) {
            throttle.fallito(username);
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "login", "Accesso rifiutato per " + username);
            throw new BadCredentialsException(ErrorConstants.AUTH_CREDENZIALI_NON_VALIDE);
        }
        throttle.riuscito(username);
        utente.setUltimoAccesso(LocalDateTime.now());
        return jwt.emetti(utente);
    }

    public UtenteResponse me(String username) {
        return UtenteService.toResponse(trova(username));
    }

    @Transactional
    public void cambiaPassword(String username, CambioPasswordRequest dto) {
        Utente utente = trova(username);
        if (!encoder.matches(dto.passwordAttuale(), utente.getPasswordHash())) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "cambiaPassword", ErrorConstants.UTENTE_PASSWORD_ATTUALE_ERRATA);
            throw new IllegalArgumentException(ErrorConstants.UTENTE_PASSWORD_ATTUALE_ERRATA);
        }
        if (!PasswordPolicy.valida(dto.nuovaPassword(), utente.getUsername())) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "cambiaPassword", ErrorConstants.UTENTE_PASSWORD_DEBOLE);
            throw new IllegalArgumentException(ErrorConstants.UTENTE_PASSWORD_DEBOLE);
        }
        utente.setPasswordHash(encoder.encode(dto.nuovaPassword()));
        utente.setPasswordModificataIl(LocalDateTime.now());
    }

    private Utente trova(String username) {
        return repo.findByUsernameIgnoreCase(username).orElseThrow(() -> {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "me", ErrorConstants.UTENTE_NON_TROVATO + username);
            return new ResourceNotFoundException(ErrorConstants.UTENTE_NON_TROVATO + username);
        });
    }
}
