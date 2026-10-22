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

    // ------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------
    @Transactional
    public RicambioResponse create(RicambioRequest dto) {
        // 1. L'intervento deve esistere
        if (!interventiRepo.existsById(dto.interventoId())) {
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + dto.interventoId());
        }

        Ricambi e = new Ricambi();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    // ------------------------------------------------------------
    // READ
    // ------------------------------------------------------------
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

    // ------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------
    @Transactional
    public RicambioResponse update(Long id, RicambioRequest dto) {
        Ricambi e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ricambio non trovato con ID " + id));

        // Se l'utente sta cambiando intervento, il nuovo deve esistere
        if (!e.getInterventoId().equals(dto.interventoId())
                && !interventiRepo.existsById(dto.interventoId())) {
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + dto.interventoId());
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    // ------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------
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

    // ------------------------------------------------------------
    // OPERAZIONI AGGREGATE / BATCH
    // ------------------------------------------------------------

    /**
     * Sostituisce in blocco i ricambi di un intervento.
     * Usato tipicamente dal SyncService o da una UI "modifica ricambi".
     */
    @Transactional
    public List<RicambioResponse> replaceByIntervento(Long interventoId,
                                                      List<RicambioRequest> nuovi) {
        if (!interventiRepo.existsById(interventoId)) {
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + interventoId);
        }
        repo.deleteByInterventoId(interventoId);

        return nuovi.stream()
                .map(r -> {
                    // Forza l'interventoId dal path (evita incongruenze client)
                    RicambioRequest fixed = new RicambioRequest(
                            interventoId,
                            r.articoloId(),
                            r.codiceRicambio(),
                            r.descrizioneRicambio(),
                            r.quantitaUtilizzata(),
                            r.prezzoUnitario()
                    );
                    Ricambi e = new Ricambi();
                    apply(fixed, e);
                    return toResponse(repo.save(e));
                })
                .toList();
    }

    /** Totale pezzi usati per un articolo (su tutti gli interventi). */
    public Integer totalePezziUsati(Long articoloId) {
        return repo.sumQuantitaByArticoloId(articoloId);
    }

    /** Totale costo ricambi di un intervento. */
    public Double totaleCostoRicambi(Long interventoId) {
        if (!interventiRepo.existsById(interventoId)) {
            throw new ResourceNotFoundException(
                    "Intervento non trovato con ID " + interventoId);
        }
        return repo.sumCostoByInterventoId(interventoId);
    }

    // ------------------------------------------------------------
    // MAPPER PRIVATI
    // ------------------------------------------------------------
    private void apply(RicambioRequest dto, Ricambi e) {
        e.setInterventoId(dto.interventoId());
        e.setArticoloId(dto.articoloId());
        e.setCodiceRicambio(dto.codiceRicambio());
        e.setDescrizioneRicambio(dto.descrizioneRicambio());
        e.setQuantitaUtilizzata(dto.quantitaUtilizzata());
        e.setPrezzoUnitario(dto.prezzoUnitario());
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