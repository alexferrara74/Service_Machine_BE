package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pilot.servizi.dto.response.PageResponse;
import pilot.servizi.dto.response.RegistroAttivitaResponse;
import pilot.servizi.entity.RegistroAttivita;
import pilot.servizi.entity.RegistroAttivita.Azione;
import pilot.servizi.entity.RegistroAttivita.Esito;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.repository.RegistroAttivitaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Registro delle attività. Scrivere nel registro non deve mai far fallire l'operazione dell'utente:
 * gli errori di salvataggio vengono solo loggati.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistroService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    private final RegistroAttivitaRepository repo;

    // ---------- scrittura ----------

    /** Operazione HTTP di scrittura (o esportazione) eseguita da un utente autenticato. */
    @Transactional
    public void registraRichiesta(String username, String ruolo, String metodo, String percorso, int status, String ip) {
        Azione azione = azione(metodo, percorso);
        if (azione == null) {
            return;
        }
        Percorso p = analizza(percorso);
        salva(RegistroAttivita.builder()
                .quando(LocalDateTime.now())
                .username(username)
                .ruolo(ruolo)
                .azione(azione.name())
                .risorsa(troncato(p.risorsa(), 80))
                .risorsaId(troncato(p.id(), 40))
                .esito(esito(status).name())
                .codiceHttp(status)
                .ip(troncato(ip, 45))
                .dettaglio(troncato(p.dettaglio(), 255))
                .build());
    }

    /**
     * Evento di autenticazione. Transazione propria: i tentativi falliti vengono registrati anche se
     * l'operazione chiamante (il login) viene annullata con un'eccezione.
     * Lo username dei tentativi falliti è salvato solo se ha la forma di uno username.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registraAccesso(Azione azione, String username, String ruolo, Esito esito, String dettaglio) {
        salva(RegistroAttivita.builder()
                .quando(LocalDateTime.now())
                // Chi sbaglia può aver scritto la password nel campo username: se non sembra uno username non lo conserviamo.
                .username(usernameSicuro(username))
                .ruolo(ruolo)
                .azione(azione.name())
                .risorsa("auth")
                .esito(esito.name())
                .ip(troncato(ipCorrente(), 45))
                .dettaglio(troncato(dettaglio, 255))
                .build());
    }

    /** Lo username se ha davvero la forma di uno username, altrimenti null: chi sbaglia può aver scritto la password nel campo. */
    public static String usernameSicuro(String username) {
        return username != null && USERNAME.matcher(username.trim()).matches() ? username.trim() : null;
    }

    private void salva(RegistroAttivita r) {
        try {
            repo.save(r);
        } catch (RuntimeException e) {
            log.error(ErrorConstants.LOG_UNEXPECTED, ErrorConstants.ERR_INTERNAL, "registro", "Impossibile scrivere nel registro: " + e.getMessage());
        }
    }

    // ---------- lettura ----------

    public PageResponse<RegistroAttivitaResponse> cerca(String username, String azione, String esito,
                                                        LocalDateTime da, LocalDateTime a, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "registro", ErrorConstants.NOLEGGIO_PAGINAZIONE_INVALIDA);
            throw new IllegalArgumentException(ErrorConstants.NOLEGGIO_PAGINAZIONE_INVALIDA);
        }
        if (da != null && a != null && !da.isBefore(a)) {
            throw new IllegalArgumentException(ErrorConstants.APPUNTAMENTO_INTERVALLO_INVALIDO);
        }
        String u = username == null || username.isBlank() ? null : username.trim();
        String az = valido(azione, Azione.class);
        String es = valido(esito, Esito.class);
        return PageResponse.of(repo.cerca(u, az, es, da, a,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "quando", "id")))
                .map(RegistroService::toResponse));
    }

    private static <E extends Enum<E>> String valido(String value, Class<E> tipo) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(tipo, value.trim().toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valore non valido per il filtro: " + value);
        }
    }

    // ---------- classificazione (visibile ai test) ----------

    static Azione azione(String metodo, String percorso) {
        return switch (metodo.toUpperCase()) {
            case "POST" -> percorso.equals("/api/auth/password") ? Azione.CAMBIO_PASSWORD : Azione.CREA;
            case "PUT", "PATCH" -> Azione.MODIFICA;
            case "DELETE" -> Azione.ELIMINA;
            case "GET" -> percorso.contains("/export") ? Azione.ESPORTA : null;
            default -> null;
        };
    }

    static Esito esito(int status) {
        if (status >= 200 && status < 400) return Esito.OK;
        if (status == 401 || status == 403) return Esito.NEGATO;
        if (status >= 500) return Esito.ERRORE;
        return Esito.RIFIUTATO;
    }

    record Percorso(String risorsa, String id, String dettaglio) {}

    /** /api/storia/noleggi/12/manutenzione → risorsa "storia/noleggi", id "12", dettaglio "manutenzione". */
    static Percorso analizza(String percorso) {
        String senzaApi = percorso.startsWith("/api/") ? percorso.substring(5) : percorso;
        List<String> risorsa = new ArrayList<>();
        List<String> resto = new ArrayList<>();
        String id = null;
        for (String segmento : senzaApi.split("/")) {
            if (segmento.isEmpty()) continue;
            if (id == null && segmento.chars().allMatch(Character::isDigit)) {
                id = segmento;
            } else if (id == null) {
                risorsa.add(segmento);
            } else {
                resto.add(segmento);
            }
        }
        return new Percorso(String.join("/", risorsa), id, resto.isEmpty() ? null : String.join("/", resto));
    }

    private static String troncato(String s, int max) {
        return s == null ? null : s.length() <= max ? s : s.substring(0, max);
    }

    /** Indirizzo del client così come lo vede il server: dietro un proxy è quello del proxy. */
    private static String ipCorrente() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a ? a.getRequest().getRemoteAddr() : null;
    }

    static RegistroAttivitaResponse toResponse(RegistroAttivita r) {
        return new RegistroAttivitaResponse(r.getId(), r.getQuando(), r.getUsername(), r.getRuolo(), r.getAzione(),
                r.getRisorsa(), r.getRisorsaId(), r.getEsito(), r.getCodiceHttp(), r.getIp(), r.getDettaglio());
    }
}
