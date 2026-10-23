package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.ClienteRequest;
import pilot.servizi.dto.response.ClienteResponse;
import pilot.servizi.entity.Clienti;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientiService {

    private final ClientiRepository repo;

    @Transactional
    public ClienteResponse create(ClienteRequest dto) {
        validateRequest(dto, "create");

        if (repo.existsByCodice(dto.codice().trim())) {
            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "create", ErrorConstants.CLIENTE_CODICE_DUPLICATO + dto.codice());
            throw new DataIntegrityViolationException(ErrorConstants.CLIENTE_CODICE_DUPLICATO + dto.codice());
        }

        Clienti e = new Clienti();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public ClienteResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getById", ErrorConstants.CLIENTE_NON_TROVATO + id);
                    return new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + id);
                }));
    }

    public List<ClienteResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ClienteResponse update(Long id, ClienteRequest dto) {
        validateRequest(dto, "update");

        Clienti e = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.CLIENTE_NON_TROVATO + id);
                    return new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + id);
                });

        repo.findByCodice(dto.codice().trim()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "update", ErrorConstants.CLIENTE_CODICE_DUPLICATO + dto.codice());
                throw new DataIntegrityViolationException(ErrorConstants.CLIENTE_CODICE_DUPLICATO + dto.codice());
            }
        });

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "delete", ErrorConstants.CLIENTE_NON_TROVATO + id);
            throw new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + id);
        }
        repo.deleteById(id);
    }

    private void validateRequest(ClienteRequest dto, String operation) {
        if (dto == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.CLIENTE_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.CLIENTE_DTO_NULL);
        }
        if (dto.codice() == null || dto.codice().trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.CLIENTE_CODICE_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.CLIENTE_CODICE_OBBLIGATORIO);
        }
        if (dto.codice().trim().length() > 50) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.CLIENTE_CODICE_TOO_LONG);
            throw new IllegalArgumentException(ErrorConstants.CLIENTE_CODICE_TOO_LONG);
        }
        if (dto.ragioneSociale() == null || dto.ragioneSociale().trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.CLIENTE_RAGIONE_SOCIALE_OBBLIGATORIA);
            throw new IllegalArgumentException(ErrorConstants.CLIENTE_RAGIONE_SOCIALE_OBBLIGATORIA);
        }
        if (dto.ragioneSociale().trim().length() > 255) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.CLIENTE_RAGIONE_SOCIALE_TOO_LONG);
            throw new IllegalArgumentException(ErrorConstants.CLIENTE_RAGIONE_SOCIALE_TOO_LONG);
        }
        if (dto.partitaIva() != null && dto.partitaIva().trim().length() > 20) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.CLIENTE_PIVA_TOO_LONG);
            throw new IllegalArgumentException(ErrorConstants.CLIENTE_PIVA_TOO_LONG);
        }
        if (dto.codiceFiscale() != null && dto.codiceFiscale().trim().length() > 20) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.CLIENTE_CF_TOO_LONG);
            throw new IllegalArgumentException(ErrorConstants.CLIENTE_CF_TOO_LONG);
        }
        if (dto.email() != null && !dto.email().isBlank() && (!dto.email().contains("@") || !dto.email().contains("."))) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.CLIENTE_EMAIL_INVALID);
            throw new IllegalArgumentException(ErrorConstants.CLIENTE_EMAIL_INVALID);
        }
    }

    private void apply(ClienteRequest dto, Clienti e) {
        e.setCodice(dto.codice() != null ? dto.codice().trim() : null);
        e.setRagioneSociale(dto.ragioneSociale() != null ? dto.ragioneSociale().trim() : null);
        e.setNome(dto.nome());
        e.setCognome(dto.cognome());
        e.setTelefono(dto.telefono());
        e.setEmail(dto.email());
        e.setPartitaIva(dto.partitaIva());
        e.setCodiceFiscale(dto.codiceFiscale());
        e.setTipoListino(dto.tipoListino());
    }

    private ClienteResponse toResponse(Clienti e) {
        return new ClienteResponse(
                e.getId(),
                e.getCodice(),
                e.getRagioneSociale(),
                e.getNome(),
                e.getCognome(),
                e.getTelefono(),
                e.getEmail(),
                e.getPartitaIva(),
                e.getCodiceFiscale(),
                e.getTipoListino(),
                e.getAggiornatoIl()
        );
    }
}
