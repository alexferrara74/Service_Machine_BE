package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.MagazzinoRequest;
import pilot.servizi.dto.response.MagazzinoResponse;
import pilot.servizi.entity.MagazzinoDigitale;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.MagazzinoDigitaleRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MagazzinoDigitaleService {

    private final MagazzinoDigitaleRepository repo;

    @Transactional
    public MagazzinoResponse create(MagazzinoRequest dto) {
        validateRequest(dto, "create");

        if (repo.existsByCodiceUbicazione(dto.codiceUbicazione().trim())) {
            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "create", ErrorConstants.MAGAZZINO_CODICE_DUPLICATO + dto.codiceUbicazione());
            throw new DataIntegrityViolationException(ErrorConstants.MAGAZZINO_CODICE_DUPLICATO + dto.codiceUbicazione());
        }

        MagazzinoDigitale e = new MagazzinoDigitale();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public MagazzinoResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getById", ErrorConstants.MAGAZZINO_NON_TROVATO + id);
                    return new ResourceNotFoundException(ErrorConstants.MAGAZZINO_NON_TROVATO + id);
                }));
    }

    public MagazzinoResponse getByCodiceUbicazione(String codiceUbicazione) {
        if (codiceUbicazione == null || codiceUbicazione.trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "getByCodiceUbicazione", ErrorConstants.MAGAZZINO_CODICE_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.MAGAZZINO_CODICE_OBBLIGATORIO);
        }
        return toResponse(repo.findByCodiceUbicazione(codiceUbicazione.trim())
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getByCodiceUbicazione", "Ubicazione non trovata con codice: " + codiceUbicazione);
                    return new ResourceNotFoundException("Ubicazione non trovata con codice: " + codiceUbicazione);
                }));
    }

    public List<MagazzinoResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public List<MagazzinoResponse> getByZona(String zona) {
        return repo.findByZona(zona).stream().map(this::toResponse).toList();
    }

    public List<MagazzinoResponse> getByZonaEScaffale(String zona, String scaffale) {
        return repo.findByZonaAndScaffale(zona, scaffale).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MagazzinoResponse update(Long id, MagazzinoRequest dto) {
        validateRequest(dto, "update");

        MagazzinoDigitale e = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.MAGAZZINO_NON_TROVATO + id);
                    return new ResourceNotFoundException(ErrorConstants.MAGAZZINO_NON_TROVATO + id);
                });

        if (!e.getCodiceUbicazione().equalsIgnoreCase(dto.codiceUbicazione().trim())) {
            repo.findByCodiceUbicazione(dto.codiceUbicazione().trim())
                    .ifPresent(other -> {
                        if (!other.getUbicazioneId().equals(id)) {
                            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "update", ErrorConstants.MAGAZZINO_CODICE_DUPLICATO + dto.codiceUbicazione());
                            throw new DataIntegrityViolationException(ErrorConstants.MAGAZZINO_CODICE_DUPLICATO + dto.codiceUbicazione());
                        }
                    });
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "delete", ErrorConstants.MAGAZZINO_NON_TROVATO + id);
            throw new ResourceNotFoundException(ErrorConstants.MAGAZZINO_NON_TROVATO + id);
        }
        repo.deleteById(id);
    }

    private void validateRequest(MagazzinoRequest dto, String operation) {
        if (dto == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.MAGAZZINO_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.MAGAZZINO_DTO_NULL);
        }
        if (dto.codiceUbicazione() == null || dto.codiceUbicazione().trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.MAGAZZINO_CODICE_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.MAGAZZINO_CODICE_OBBLIGATORIO);
        }
        if (dto.codiceUbicazione().trim().length() > 50) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.MAGAZZINO_CODICE_TOO_LONG);
            throw new IllegalArgumentException(ErrorConstants.MAGAZZINO_CODICE_TOO_LONG);
        }
        if (dto.zona() == null || dto.zona().trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.MAGAZZINO_ZONA_OBBLIGATORIA);
            throw new IllegalArgumentException(ErrorConstants.MAGAZZINO_ZONA_OBBLIGATORIA);
        }
        if (dto.capacitaMax() != null && dto.capacitaMax() < 1) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.MAGAZZINO_CAPACITA_INVALIDA);
            throw new IllegalArgumentException(ErrorConstants.MAGAZZINO_CAPACITA_INVALIDA);
        }
    }

    private void apply(MagazzinoRequest dto, MagazzinoDigitale e) {
        e.setCodiceUbicazione(dto.codiceUbicazione().trim());
        e.setZona(dto.zona().trim());
        e.setScaffale(dto.scaffale());
        e.setRipiano(dto.ripiano());
        e.setCapacitaMax(dto.capacitaMax());
        e.setNote(dto.note());
    }

    private MagazzinoResponse toResponse(MagazzinoDigitale e) {
        return new MagazzinoResponse(
                e.getUbicazioneId(),
                e.getCodiceUbicazione(),
                e.getZona(),
                e.getScaffale(),
                e.getRipiano(),
                e.getCapacitaMax(),
                e.getNote(),
                e.getCreatoIl(),
                e.getAggiornatoIl()
        );
    }
}
