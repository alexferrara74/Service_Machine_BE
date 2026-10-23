package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.RicambioRequest;
import pilot.servizi.dto.response.RicambioResponse;
import pilot.servizi.entity.Ricambi;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.InterventiRepository;
import pilot.servizi.repository.RicambisticaInterventiRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RicambisticaInterventiService {

    private final RicambisticaInterventiRepository repo;
    private final InterventiRepository interventiRepo;

    @Transactional
    public RicambioResponse create(RicambioRequest dto) {
        validateRequest(dto);

        if (!interventiRepo.existsById(dto.interventoId())) {
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + dto.interventoId());
        }

        Ricambi e = new Ricambi();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public RicambioResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ricambio non trovato con ID " + id)));
    }

    public List<RicambioResponse> getByIntervento(Long interventoId) {
        if (!interventiRepo.existsById(interventoId)) {
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + interventoId);
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
        validateRequest(dto);

        Ricambi e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ricambio non trovato con ID " + id));

        if (!e.getInterventoId().equals(dto.interventoId())
                && !interventiRepo.existsById(dto.interventoId())) {
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + dto.interventoId());
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Ricambio non trovato con ID " + id);
        }
        repo.deleteById(id);
    }

    @Transactional
    public void deleteByIntervento(Long interventoId) {
        if (!interventiRepo.existsById(interventoId)) {
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + interventoId);
        }
        repo.deleteByInterventoId(interventoId);
    }

    @Transactional
    public List<RicambioResponse> replaceByIntervento(Long interventoId,
                                                      List<RicambioRequest> nuovi) {
        if (!interventiRepo.existsById(interventoId)) {
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + interventoId);
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
                    validateRequest(fixed);
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
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + interventoId);
        }
        return repo.sumCostoByInterventoId(interventoId);
    }

    private void validateRequest(RicambioRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati del ricambio non possono essere null");
        }
        if (dto.interventoId() == null) {
            throw new IllegalArgumentException("L'ID intervento è obbligatorio");
        }
        if (dto.articoloId() == null) {
            throw new IllegalArgumentException("L'ID articolo è obbligatorio");
        }
        if (dto.quantitaUtilizzata() == null || dto.quantitaUtilizzata() < 1) {
            throw new IllegalArgumentException("La quantità utilizzata deve essere almeno 1");
        }
        if (dto.prezzoUnitario() != null && dto.prezzoUnitario() < 0) {
            throw new IllegalArgumentException("Il prezzo unitario non può essere negativo");
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
