package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.RicambioRequest;
import pilot.servizi.dto.response.RicambioResponse;
import pilot.servizi.entity.Ricambi;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.InterventiRepository;
import pilot.servizi.repository.RicambisticaInterventiRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RicambisticaInterventiService {

    private final RicambisticaInterventiRepository repo;
    private final InterventiRepository interventiRepo;

    @Transactional
    public RicambioResponse create(RicambioRequest dto) {
        validateRequest(dto, "create");

        if (!interventiRepo.existsById(dto.interventoId())) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "create", ErrorConstants.INTERVENTO_NON_TROVATO + dto.interventoId());
            throw new ResourceNotFoundException(ErrorConstants.INTERVENTO_NON_TROVATO + dto.interventoId());
        }

        Ricambi e = new Ricambi();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public RicambioResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getById", ErrorConstants.RICAMBIO_NON_TROVATO + id);
                    return new ResourceNotFoundException(ErrorConstants.RICAMBIO_NON_TROVATO + id);
                }));
    }

    public List<RicambioResponse> getByIntervento(Long interventoId) {
        if (!interventiRepo.existsById(interventoId)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getByIntervento", ErrorConstants.INTERVENTO_NON_TROVATO + interventoId);
            throw new ResourceNotFoundException(ErrorConstants.INTERVENTO_NON_TROVATO + interventoId);
        }
        return repo.findByInterventoId(interventoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RicambioResponse> getByArticolo(Long articoloId) {
        return repo.findByArticoloId(articoloId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RicambioResponse> getAll() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RicambioResponse update(Long id, RicambioRequest dto) {
        validateRequest(dto, "update");

        Ricambi e = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.RICAMBIO_NON_TROVATO + id);
                    return new ResourceNotFoundException(ErrorConstants.RICAMBIO_NON_TROVATO + id);
                });

        if (!e.getInterventoId().equals(dto.interventoId()) && !interventiRepo.existsById(dto.interventoId())) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.INTERVENTO_NON_TROVATO + dto.interventoId());
            throw new ResourceNotFoundException(ErrorConstants.INTERVENTO_NON_TROVATO + dto.interventoId());
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "delete", ErrorConstants.RICAMBIO_NON_TROVATO + id);
            throw new ResourceNotFoundException(ErrorConstants.RICAMBIO_NON_TROVATO + id);
        }
        repo.deleteById(id);
    }

    @Transactional
    public void deleteByIntervento(Long interventoId) {
        if (!interventiRepo.existsById(interventoId)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "deleteByIntervento", ErrorConstants.INTERVENTO_NON_TROVATO + interventoId);
            throw new ResourceNotFoundException(ErrorConstants.INTERVENTO_NON_TROVATO + interventoId);
        }
        repo.deleteByInterventoId(interventoId);
    }

    @Transactional
    public List<RicambioResponse> replaceByIntervento(Long interventoId, List<RicambioRequest> nuovi) {
        if (!interventiRepo.existsById(interventoId)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "replaceByIntervento", ErrorConstants.INTERVENTO_NON_TROVATO + interventoId);
            throw new ResourceNotFoundException(ErrorConstants.INTERVENTO_NON_TROVATO + interventoId);
        }
        repo.deleteByInterventoId(interventoId);

        if (nuovi == null || nuovi.isEmpty()) {
            return List.of();
        }

        return nuovi.stream()
                .map(r -> {
                    RicambioRequest fixed = new RicambioRequest(
                            interventoId,
                            r.articoloId(),
                            r.codiceRicambio(),
                            r.descrizioneRicambio(),
                            r.quantitaUtilizzata(),
                            r.prezzoUnitario()
                    );
                    validateRequest(fixed, "replaceByIntervento");
                    Ricambi e = new Ricambi();
                    apply(fixed, e);
                    return toResponse(repo.save(e));
                })
                .toList();
    }

    public Integer totalePezziUsati(Long articoloId) {
        return repo.sumQuantitaByArticoloId(articoloId);
    }

    public Double totaleCostoRicambi(Long interventoId) {
        if (!interventiRepo.existsById(interventoId)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "totaleCostoRicambi", ErrorConstants.INTERVENTO_NON_TROVATO + interventoId);
            throw new ResourceNotFoundException(ErrorConstants.INTERVENTO_NON_TROVATO + interventoId);
        }
        return repo.sumCostoByInterventoId(interventoId);
    }

    private void validateRequest(RicambioRequest dto, String operation) {
        if (dto == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.RICAMBIO_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.RICAMBIO_DTO_NULL);
        }
        if (dto.interventoId() == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.RICAMBIO_INTERVENTO_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.RICAMBIO_INTERVENTO_OBBLIGATORIO);
        }
        if (dto.articoloId() == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.RICAMBIO_ARTICOLO_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.RICAMBIO_ARTICOLO_OBBLIGATORIO);
        }
        if (dto.quantitaUtilizzata() == null || dto.quantitaUtilizzata() < 1) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.RICAMBIO_QUANTITA_INVALIDA);
            throw new IllegalArgumentException(ErrorConstants.RICAMBIO_QUANTITA_INVALIDA);
        }
        if (dto.prezzoUnitario() != null && dto.prezzoUnitario() < 0) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.RICAMBIO_PREZZO_NEGATIVO);
            throw new IllegalArgumentException(ErrorConstants.RICAMBIO_PREZZO_NEGATIVO);
        }
    }

    private void apply(RicambioRequest dto, Ricambi e) {
        e.setInterventoId(dto.interventoId());
        e.setArticoloId(dto.articoloId());
        e.setCodiceRicambio(dto.codiceRicambio());
        e.setDescrizioneRicambio(dto.descrizioneRicambio());
        e.setQuantitaUtilizzata(dto.quantitaUtilizzata());
        e.setPrezzoUnitario(dto.prezzoUnitario() != null ? dto.prezzoUnitario() : 0.0);
    }

    private RicambioResponse toResponse(Ricambi e) {
        return new RicambioResponse(
                e.getId(),
                e.getInterventoId(),
                e.getArticoloId(),
                e.getCodiceRicambio(),
                e.getDescrizioneRicambio(),
                e.getQuantitaUtilizzata(),
                e.getPrezzoUnitario()
        );
    }
}
