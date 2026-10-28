package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.StoriaNoleggioRequest;
import pilot.servizi.dto.response.StoriaNoleggioResponse;
import pilot.servizi.dto.response.PageResponse;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AttrezzatureRepository;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.ProdottiCatalogoRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoriaNoleggiService {

    private static final int MAX_PAGE_SIZE = 200;

    private final StoriaNoleggiRepository repo;
    private final ClientiRepository clientiRepo;
    private final ProdottiCatalogoRepository prodottiRepo;
    private final AttrezzatureRepository attrezzatureRepo;

    @Transactional
    public StoriaNoleggioResponse create(StoriaNoleggioRequest dto) {
        validateRequest(dto, "create");
        validateReferences(dto, "create");

        StoriaNoleggi e = new StoriaNoleggi();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public StoriaNoleggioResponse getById(Integer docId) {
        return toResponse(repo.findById(docId)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getById", ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
                    return new ResourceNotFoundException(ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
                }));
    }

    public List<StoriaNoleggioResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public List<StoriaNoleggioResponse> getByCliente(Long clienteId) {
        return repo.findByClienteId(clienteId).stream().map(this::toResponse).toList();
    }

    public List<StoriaNoleggioResponse> getByArticolo(Long articoloId) {
        return repo.findByArticoloId(articoloId).stream().map(this::toResponse).toList();
    }

    public List<StoriaNoleggioResponse> getByMatricola(String matricola) {
        return repo.findByMatricola(matricola).stream().map(this::toResponse).toList();
    }

    public PageResponse<StoriaNoleggioResponse> getPage(int page, int size) {
        return toPageResponse(repo.findAll(pageRequest(page, size)));
    }

    public PageResponse<StoriaNoleggioResponse> getPageByCliente(Long clienteId, int page, int size) {
        return toPageResponse(repo.findByClienteId(clienteId, pageRequest(page, size)));
    }

    public PageResponse<StoriaNoleggioResponse> getPageByArticolo(Long articoloId, int page, int size) {
        return toPageResponse(repo.findByArticoloId(articoloId, pageRequest(page, size)));
    }

    public PageResponse<StoriaNoleggioResponse> getPageByMatricola(String matricola, int page, int size) {
        return toPageResponse(repo.findByMatricola(matricola, pageRequest(page, size)));
    }

    @Transactional
    public StoriaNoleggioResponse update(Integer docId, StoriaNoleggioRequest dto) {
        validateRequest(dto, "update");

        StoriaNoleggi e = repo.findById(docId)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
                    return new ResourceNotFoundException(ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
                });

        validateReferences(dto, "update");

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Integer docId) {
        if (!repo.existsById(docId)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "delete", ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
            throw new ResourceNotFoundException(ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
        }
        repo.deleteById(docId);
    }

    private void validateRequest(StoriaNoleggioRequest dto, String operation) {
        if (dto == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.NOLEGGIO_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.NOLEGGIO_DTO_NULL);
        }
        if (dto.clienteId() == null) {
            throw invalid(operation, ErrorConstants.NOLEGGIO_CLIENTE_OBBLIGATORIO);
        }
        if (dto.articoloId() == null) {
            throw invalid(operation, ErrorConstants.NOLEGGIO_ARTICOLO_OBBLIGATORIO);
        }
        if (dto.dataDoc() == null) {
            throw invalid(operation, ErrorConstants.NOLEGGIO_DATA_OBBLIGATORIA);
        }
        if (dto.matricola() == null || dto.matricola().trim().isEmpty()) {
            throw invalid(operation, ErrorConstants.NOLEGGIO_MATRICOLA_OBBLIGATORIA);
        }
        if (dto.matricola().trim().length() > 100) {
            throw invalid(operation, ErrorConstants.NOLEGGIO_MATRICOLA_TOO_LONG);
        }
        if (dto.sezionale() != null && dto.sezionale().length() > 50) {
            throw invalid(operation, ErrorConstants.NOLEGGIO_SEZIONALE_TOO_LONG);
        }
        if (dto.quantita() != null && dto.quantita() < 0) {
            throw invalid(operation, ErrorConstants.NOLEGGIO_QUANTITA_NEGATIVA);
        }
    }

    private void validateReferences(StoriaNoleggioRequest dto, String operation) {
        if (!clientiRepo.existsById(dto.clienteId())) {
            throw notFound(operation, ErrorConstants.CLIENTE_NON_TROVATO + dto.clienteId());
        }
        if (!prodottiRepo.existsById(dto.articoloId())) {
            throw notFound(operation, ErrorConstants.NOLEGGIO_ARTICOLO_NON_TROVATO + dto.articoloId());
        }
        String matricola = dto.matricola().trim();
        if (!attrezzatureRepo.existsByMatricola(matricola)) {
            throw notFound(operation, ErrorConstants.ATTREZZATURA_NON_TROVATA_MATRICOLA + matricola);
        }
    }

    private IllegalArgumentException invalid(String operation, String message) {
        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, message);
        return new IllegalArgumentException(message);
    }

    private ResourceNotFoundException notFound(String operation, String message) {
        log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, operation, message);
        return new ResourceNotFoundException(message);
    }

    private void apply(StoriaNoleggioRequest dto, StoriaNoleggi e) {
        e.setSezionale(dto.sezionale());
        e.setDataDoc(dto.dataDoc());
        e.setClienteId(dto.clienteId());
        e.setArticoloId(dto.articoloId());
        e.setMatricola(dto.matricola().trim());
        e.setQuantita(dto.quantita());
        prodottiRepo.findById(dto.articoloId()).ifPresent(p -> {
            e.setCodiceArticolo(p.getCodice());
            e.setDescrizione(p.getDescrizione());
        });
    }

    private StoriaNoleggioResponse toResponse(StoriaNoleggi e) {
        return new StoriaNoleggioResponse(
                e.getDocId(),
                e.getSezionale(),
                e.getDataDoc(),
                e.getClienteId(),
                e.getArticoloId(),
                e.getMatricola(),
                e.getQuantita(),
                e.getCodiceArticolo(),
                e.getDescrizione(),
                e.getOrigine(),
                e.getAggiornatoIl()
        );
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "getPage",
                    ErrorConstants.NOLEGGIO_PAGINAZIONE_INVALIDA);
            throw new IllegalArgumentException(ErrorConstants.NOLEGGIO_PAGINAZIONE_INVALIDA);
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "docId"));
    }

    private PageResponse<StoriaNoleggioResponse> toPageResponse(Page<StoriaNoleggi> page) {
        return PageResponse.of(page.map(this::toResponse));
    }
}
