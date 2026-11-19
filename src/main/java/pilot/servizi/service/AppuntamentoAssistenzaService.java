package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.AppuntamentoRequest;
import pilot.servizi.dto.response.AppuntamentoResponse;
import pilot.servizi.entity.AppuntamentoAssistenza;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AppuntamentoAssistenzaRepository;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppuntamentoAssistenzaService {

    private final AppuntamentoAssistenzaRepository repo;
    private final StoriaNoleggiRepository noleggiRepo;
    private final ClientiRepository clientiRepo;

    /** Appuntamenti che toccano l'intervallo [da, a), ordinati per inizio: è la query del calendario. */
    public List<AppuntamentoResponse> getByIntervallo(LocalDateTime da, LocalDateTime a) {
        if (da == null || a == null || !da.isBefore(a)) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "getByIntervallo", ErrorConstants.APPUNTAMENTO_INTERVALLO_INVALIDO);
            throw new IllegalArgumentException(ErrorConstants.APPUNTAMENTO_INTERVALLO_INVALIDO);
        }
        return toResponses(repo.findByInizioLessThanAndFineGreaterThanOrderByInizioAsc(a, da));
    }

    public List<AppuntamentoResponse> getByNoleggio(Integer docId) {
        return toResponses(repo.findByDocIdOrderByInizioAsc(docId));
    }

    public AppuntamentoResponse getById(Long id) {
        return toResponses(List.of(find(id, "getById"))).get(0);
    }

    @Transactional
    public AppuntamentoResponse create(AppuntamentoRequest dto) {
        validateRequest(dto, "create");
        checkNoleggio(dto, "create");
        AppuntamentoAssistenza e = new AppuntamentoAssistenza();
        apply(dto, e);
        return toResponses(List.of(repo.save(e))).get(0);
    }

    @Transactional
    public AppuntamentoResponse update(Long id, AppuntamentoRequest dto) {
        validateRequest(dto, "update");
        AppuntamentoAssistenza e = find(id, "update");
        checkNoleggio(dto, "update");
        apply(dto, e);
        return toResponses(List.of(repo.save(e))).get(0);
    }

    /** Cambio rapido di stato (conferma, esecuzione, annullamento) senza rimandare tutto l'appuntamento. */
    @Transactional
    public AppuntamentoResponse updateStato(Long id, AppuntamentoAssistenza.Stato stato) {
        if (stato == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "updateStato", ErrorConstants.APPUNTAMENTO_STATO_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.APPUNTAMENTO_STATO_OBBLIGATORIO);
        }
        AppuntamentoAssistenza e = find(id, "updateStato");
        e.setStato(stato);
        return toResponses(List.of(repo.save(e))).get(0);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "delete", ErrorConstants.APPUNTAMENTO_NON_TROVATO + id);
            throw new ResourceNotFoundException(ErrorConstants.APPUNTAMENTO_NON_TROVATO + id);
        }
        repo.deleteById(id);
    }

    private AppuntamentoAssistenza find(Long id, String operation) {
        return repo.findById(id).orElseThrow(() -> {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, operation, ErrorConstants.APPUNTAMENTO_NON_TROVATO + id);
            return new ResourceNotFoundException(ErrorConstants.APPUNTAMENTO_NON_TROVATO + id);
        });
    }

    private void checkNoleggio(AppuntamentoRequest dto, String operation) {
        if (dto.docId() != null && !noleggiRepo.existsById(dto.docId())) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, operation, ErrorConstants.NOLEGGIO_NON_TROVATO + dto.docId());
            throw new ResourceNotFoundException(ErrorConstants.NOLEGGIO_NON_TROVATO + dto.docId());
        }
    }

    private void validateRequest(AppuntamentoRequest dto, String operation) {
        if (dto == null) {
            fail(operation, ErrorConstants.APPUNTAMENTO_DTO_NULL);
        }
        if (isBlank(dto.titolo())) {
            fail(operation, ErrorConstants.APPUNTAMENTO_TITOLO_OBBLIGATORIO);
        }
        if (dto.stato() == null) {
            fail(operation, ErrorConstants.APPUNTAMENTO_STATO_OBBLIGATORIO);
        }
        if (dto.inizio() == null || dto.fine() == null) {
            fail(operation, ErrorConstants.APPUNTAMENTO_DATE_OBBLIGATORIE);
        }
        if (!dto.fine().isAfter(dto.inizio())) {
            fail(operation, ErrorConstants.APPUNTAMENTO_DATE_INCOERENTI);
        }
        if (dto.docId() == null && isBlank(dto.clienteNome())) {
            fail(operation, ErrorConstants.APPUNTAMENTO_CLIENTE_OBBLIGATORIO);
        }
        checkLength(dto.titolo(), 255, "titolo", operation);
        checkLength(dto.clienteNome(), 255, "clienteNome", operation);
        checkLength(dto.clienteTelefono(), 50, "clienteTelefono", operation);
        checkLength(dto.indirizzo(), 255, "indirizzo", operation);
        checkLength(dto.apparecchio(), 255, "apparecchio", operation);
        checkLength(dto.tecnico(), 100, "tecnico", operation);
    }

    private void checkLength(String value, int max, String field, String operation) {
        if (value != null && value.trim().length() > max) {
            fail(operation, ErrorConstants.APPUNTAMENTO_CAMPO_TOO_LONG + field);
        }
    }

    private void fail(String operation, String message) {
        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, message);
        throw new IllegalArgumentException(message);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String clean(String s) {
        return isBlank(s) ? null : s.trim();
    }

    private void apply(AppuntamentoRequest dto, AppuntamentoAssistenza e) {
        boolean censito = dto.docId() != null;
        e.setDocId(dto.docId());
        // Per un noleggio censito cliente e telefono arrivano dall'anagrafica: non si duplicano nell'appuntamento.
        e.setClienteNome(censito ? null : clean(dto.clienteNome()));
        e.setClienteTelefono(censito ? null : clean(dto.clienteTelefono()));
        e.setIndirizzo(clean(dto.indirizzo()));
        e.setApparecchio(clean(dto.apparecchio()));
        e.setTitolo(dto.titolo().trim());
        e.setNote(clean(dto.note()));
        e.setStato(dto.stato());
        e.setInizio(dto.inizio());
        e.setFine(dto.fine());
        e.setTecnico(clean(dto.tecnico()));
    }

    /** Risolve noleggi e clienti con due sole query per l'intero elenco (niente N+1 sul calendario). */
    private List<AppuntamentoResponse> toResponses(List<AppuntamentoAssistenza> items) {
        List<Integer> docIds = items.stream().map(AppuntamentoAssistenza::getDocId).filter(Objects::nonNull).distinct().toList();
        Map<Integer, StoriaNoleggi> noleggi = noleggiRepo.findAllById(docIds).stream()
                .collect(Collectors.toMap(StoriaNoleggi::getDocId, n -> n));
        List<Long> clienteIds = noleggi.values().stream().map(StoriaNoleggi::getClienteId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Clienti> clienti = new HashMap<>();
        clientiRepo.findAllById(clienteIds).forEach(c -> clienti.put(c.getId(), c));
        return items.stream().map(e -> toResponse(e, noleggi.get(e.getDocId()), clienti)).toList();
    }

    private AppuntamentoResponse toResponse(AppuntamentoAssistenza e, StoriaNoleggi n, Map<Long, Clienti> clienti) {
        boolean censito = e.getDocId() != null;
        Clienti c = n != null && n.getClienteId() != null ? clienti.get(n.getClienteId()) : null;
        String cliente = censito ? (c != null ? c.getRagioneSociale() : null) : e.getClienteNome();
        String telefono = censito ? (c != null ? c.getTelefono() : null) : e.getClienteTelefono();
        String apparecchio = e.getApparecchio() != null ? e.getApparecchio() : (n != null ? n.getDescrizione() : null);
        return new AppuntamentoResponse(
                e.getId(),
                e.getDocId(),
                censito,
                n != null ? n.getClienteId() : null,
                cliente,
                telefono,
                e.getIndirizzo(),
                n != null ? n.getMatricola() : null,
                apparecchio,
                e.getTitolo(),
                e.getNote(),
                e.getStato(),
                e.getInizio(),
                e.getFine(),
                e.getTecnico()
        );
    }
}
