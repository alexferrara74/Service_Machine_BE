package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.UtenteAggiornamentoRequest;
import pilot.servizi.dto.request.UtenteRequest;
import pilot.servizi.dto.response.UtenteResponse;
import pilot.servizi.entity.Utente;
import pilot.servizi.entity.Utente.Ruolo;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.UtenteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/** Gestione degli utenti da parte degli amministratori. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtenteService {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    private final UtenteRepository repo;
    private final PasswordEncoder encoder;

    public List<UtenteResponse> getAll() {
        return repo.findAllByOrderByUsernameAsc().stream().map(UtenteService::toResponse).toList();
    }

    @Transactional
    public UtenteResponse create(UtenteRequest dto) {
        if (dto == null) {
            throw invalid("create", ErrorConstants.UTENTE_DTO_NULL);
        }
        String username = dto.username() == null ? "" : dto.username().trim();
        if (!USERNAME.matcher(username).matches()) {
            throw invalid("create", ErrorConstants.UTENTE_USERNAME_NON_VALIDO);
        }
        if (dto.ruolo() == null) {
            throw invalid("create", ErrorConstants.UTENTE_RUOLO_OBBLIGATORIO);
        }
        if (!PasswordPolicy.valida(dto.password(), username)) {
            throw invalid("create", ErrorConstants.UTENTE_PASSWORD_DEBOLE);
        }
        if (repo.existsByUsernameIgnoreCase(username)) {
            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "create", ErrorConstants.UTENTE_USERNAME_DUPLICATO + username);
            throw new DataIntegrityViolationException(ErrorConstants.UTENTE_USERNAME_DUPLICATO + username);
        }
        LocalDateTime now = LocalDateTime.now();
        Utente u = Utente.builder()
                .username(username)
                .passwordHash(encoder.encode(dto.password()))
                .ruolo(dto.ruolo())
                .attivo(true)
                .passwordModificataIl(now)
                .creatoIl(now)
                .build();
        return toResponse(repo.save(u));
    }

    /** {@code attore} è lo username dell'admin che agisce: non può togliersi i permessi da solo. */
    @Transactional
    public UtenteResponse update(Long id, UtenteAggiornamentoRequest dto, String attore) {
        if (dto == null || dto.ruolo() == null || dto.attivo() == null) {
            throw invalid("update", ErrorConstants.UTENTE_RUOLO_OBBLIGATORIO);
        }
        Utente u = find(id, "update");
        boolean stessoUtente = u.getUsername().equalsIgnoreCase(attore);
        if (stessoUtente && (dto.ruolo() != u.getRuolo() || !dto.attivo())) {
            throw conflict("update", ErrorConstants.UTENTE_AUTO_MODIFICA);
        }
        boolean restaAdminAttivo = dto.ruolo() == Ruolo.ADMIN && dto.attivo();
        if (!restaAdminAttivo) {
            garantisciAltroAdmin(u, "update");
        }
        String nuovaPassword = dto.nuovaPassword() == null || dto.nuovaPassword().isEmpty() ? null : dto.nuovaPassword();
        if (nuovaPassword != null) {
            if (!PasswordPolicy.valida(nuovaPassword, u.getUsername())) {
                throw invalid("update", ErrorConstants.UTENTE_PASSWORD_DEBOLE);
            }
            u.setPasswordHash(encoder.encode(nuovaPassword));
            u.setPasswordModificataIl(LocalDateTime.now());
        }
        u.setRuolo(dto.ruolo());
        u.setAttivo(dto.attivo());
        return toResponse(repo.save(u));
    }

    @Transactional
    public void delete(Long id, String attore) {
        Utente u = find(id, "delete");
        if (u.getUsername().equalsIgnoreCase(attore)) {
            throw conflict("delete", ErrorConstants.UTENTE_AUTO_MODIFICA);
        }
        garantisciAltroAdmin(u, "delete");
        repo.delete(u);
    }

    /** Se l'utente è un admin attivo, deve esisterne almeno un altro prima di toglierlo dal gioco. */
    private void garantisciAltroAdmin(Utente u, String operation) {
        if (u.getRuolo() == Ruolo.ADMIN && u.isAttivo() && repo.countByRuoloAndAttivoTrue(Ruolo.ADMIN) <= 1) {
            throw conflict(operation, ErrorConstants.UTENTE_ULTIMO_ADMIN);
        }
    }

    private Utente find(Long id, String operation) {
        return repo.findById(id).orElseThrow(() -> {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, operation, ErrorConstants.UTENTE_NON_TROVATO + id);
            return new ResourceNotFoundException(ErrorConstants.UTENTE_NON_TROVATO + id);
        });
    }

    private IllegalArgumentException invalid(String operation, String message) {
        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, message);
        return new IllegalArgumentException(message);
    }

    private DataIntegrityViolationException conflict(String operation, String message) {
        log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, operation, message);
        return new DataIntegrityViolationException(message);
    }

    static UtenteResponse toResponse(Utente u) {
        return new UtenteResponse(u.getId(), u.getUsername(), u.getRuolo(), u.isAttivo(), u.getUltimoAccesso(), u.getCreatoIl());
    }
}
