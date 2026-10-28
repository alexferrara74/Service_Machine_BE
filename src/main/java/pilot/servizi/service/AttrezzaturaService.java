package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.AttrezzaturaRequest;
import pilot.servizi.dto.response.AttrezzaturaResponse;
import pilot.servizi.entity.Attrezzature;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AttrezzatureRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttrezzaturaService {

    private final AttrezzatureRepository repo;

    @Transactional
    public AttrezzaturaResponse create(AttrezzaturaRequest dto) {
        validateRequest(dto, "create");

        if (repo.existsByMatricola(dto.matricola().trim())) {
            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "create", ErrorConstants.ATTREZZATURA_MATRICOLA_DUPLICATA + dto.matricola());
            throw new DataIntegrityViolationException(ErrorConstants.ATTREZZATURA_MATRICOLA_DUPLICATA + dto.matricola());
        }

        Attrezzature e = new Attrezzature();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public AttrezzaturaResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getById", ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                    return new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                }));
    }

    public AttrezzaturaResponse getByMatricola(String matricola) {
        if (matricola == null || matricola.trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "getByMatricola", ErrorConstants.ATTREZZATURA_MATRICOLA_OBBLIGATORIA);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_MATRICOLA_OBBLIGATORIA);
        }
        return toResponse(repo.findByMatricola(matricola.trim())
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getByMatricola", ErrorConstants.ATTREZZATURA_NON_TROVATA_MATRICOLA + matricola);
                    return new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_MATRICOLA + matricola);
                }));
    }

    public List<AttrezzaturaResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public List<AttrezzaturaResponse> getByStato(String stato) {
        return repo.findByStato(stato).stream().map(this::toResponse).toList();
    }

    public List<AttrezzaturaResponse> getByMarca(String marca) {
        return repo.findByMarcaIgnoreCase(marca).stream().map(this::toResponse).toList();
    }

    public List<AttrezzaturaResponse> searchByDescrizione(String q) {
        return repo.findByDescrizioneContainingIgnoreCase(q).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AttrezzaturaResponse update(Long id, AttrezzaturaRequest dto) {
        validateRequest(dto, "update");

        Attrezzature e = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                    return new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                });

        if (!dto.matricola().trim().equalsIgnoreCase(e.getMatricola())) {
            repo.findByMatricola(dto.matricola().trim()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "update", ErrorConstants.ATTREZZATURA_MATRICOLA_DUPLICATA + dto.matricola());
                    throw new DataIntegrityViolationException(ErrorConstants.ATTREZZATURA_MATRICOLA_DUPLICATA + dto.matricola());
                }
            });
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "delete", ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
            throw new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
        }
        repo.deleteById(id);
    }

    private void validateRequest(AttrezzaturaRequest dto, String operation) {
        if (dto == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_DTO_NULL);
        }
        if (dto.matricola() == null || dto.matricola().trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_MATRICOLA_OBBLIGATORIA);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_MATRICOLA_OBBLIGATORIA);
        }
        if (dto.matricola().trim().length() > 100) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_MATRICOLA_TOO_LONG);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_MATRICOLA_TOO_LONG);
        }
        if (dto.descrizione() != null && dto.descrizione().length() > 255) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_DESCRIZIONE_TOO_LONG);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_DESCRIZIONE_TOO_LONG);
        }
    }

    private void apply(AttrezzaturaRequest dto, Attrezzature e) {
        e.setMatricola(dto.matricola() != null ? dto.matricola().trim() : null);
        e.setDescrizione(dto.descrizione());
        e.setModello(dto.modello());
        e.setMarca(dto.marca());
        e.setStato(dto.stato() != null && !dto.stato().isBlank() ? dto.stato() : "DISPONIBILE");
    }

    private AttrezzaturaResponse toResponse(Attrezzature e) {
        return new AttrezzaturaResponse(
                e.getId(),
                e.getMatricola(),
                e.getDescrizione(),
                e.getModello(),
                e.getMarca(),
                e.getStato(),
                e.getAggiornatoIl()
        );
    }
}
