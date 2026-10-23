package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.StoriaNoleggioRequest;
import pilot.servizi.dto.response.StoriaNoleggioResponse;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoriaNoleggiService {

    private final StoriaNoleggiRepository repo;
    private final ClientiRepository clientiRepo;

    @Transactional
    public StoriaNoleggioResponse create(StoriaNoleggioRequest dto) {
        validateRequest(dto, "create");

        if (repo.existsById(dto.docId())) {
            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "create", ErrorConstants.NOLEGGIO_DUPLICATO + dto.docId());
            throw new DataIntegrityViolationException(ErrorConstants.NOLEGGIO_DUPLICATO + dto.docId());
        }

        if (dto.clienteId() != null && !clientiRepo.existsById(dto.clienteId())) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "create", ErrorConstants.CLIENTE_NON_TROVATO + dto.clienteId());
            throw new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + dto.clienteId());
        }

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

    @Transactional
    public StoriaNoleggioResponse update(Integer docId, StoriaNoleggioRequest dto) {
        validateRequest(dto, "update");

        StoriaNoleggi e = repo.findById(docId)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
                    return new ResourceNotFoundException(ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
                });

        if (dto.clienteId() != null && !clientiRepo.existsById(dto.clienteId())) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.CLIENTE_NON_TROVATO + dto.clienteId());
            throw new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + dto.clienteId());
        }

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
        if (dto.docId() == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.NOLEGGIO_DOC_ID_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.NOLEGGIO_DOC_ID_OBBLIGATORIO);
        }
        if (dto.quantita() != null && dto.quantita() < 0) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.NOLEGGIO_QUANTITA_NEGATIVA);
            throw new IllegalArgumentException(ErrorConstants.NOLEGGIO_QUANTITA_NEGATIVA);
        }
        if (dto.matricola() != null && dto.matricola().length() > 100) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, "La matricola supera i 100 caratteri");
            throw new IllegalArgumentException("La matricola supera i 100 caratteri");
        }
    }

    private void apply(StoriaNoleggioRequest dto, StoriaNoleggi e) {
        e.setDocId(dto.docId());
        e.setSezionale(dto.sezionale());
        e.setDataDoc(dto.dataDoc());
        e.setClienteId(dto.clienteId());
        e.setArticoloId(dto.articoloId());
        e.setMatricola(dto.matricola());
        e.setQuantita(dto.quantita());
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
                e.getAggiornatoIl()
        );
    }
}
