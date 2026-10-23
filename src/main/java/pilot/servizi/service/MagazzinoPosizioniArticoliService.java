package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.PosizioneArticoloRequest;
import pilot.servizi.dto.response.PosizioneArticoloResponse;
import pilot.servizi.entity.MagazzinoDigitale;
import pilot.servizi.entity.MagazzinoPosizioniArticoli;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.MagazzinoDigitaleRepository;
import pilot.servizi.repository.MagazzinoPosizioniArticoliRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MagazzinoPosizioniArticoliService {

    private final MagazzinoPosizioniArticoliRepository repo;
    private final MagazzinoDigitaleRepository magazzinoRepo;

    @Transactional
    public PosizioneArticoloResponse create(PosizioneArticoloRequest dto) {
        validateRequest(dto);

        MagazzinoDigitale ubicazione = magazzinoRepo.findById(dto.ubicazioneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ubicazione non trovata con ID " + dto.ubicazioneId()));

        if (repo.existsByUbicazioneIdAndArticoloId(dto.ubicazioneId(), dto.articoloId())) {
            throw new DataIntegrityViolationException("Articolo " + dto.articoloId()
                    + " già presente nell'ubicazione " + dto.ubicazioneId());
        }

        MagazzinoPosizioniArticoli e = new MagazzinoPosizioniArticoli();
        apply(dto, e);
        return toResponse(repo.save(e), ubicazione.getCodiceUbicazione());
    }

    public PosizioneArticoloResponse getById(Long id) {
        MagazzinoPosizioniArticoli e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Posizione articolo non trovata con ID " + id));
        String codiceUbicazione = magazzinoRepo.findById(e.getUbicazioneId())
                .map(MagazzinoDigitale::getCodiceUbicazione).orElse(null);
        return toResponse(e, codiceUbicazione);
    }

    public List<PosizioneArticoloResponse> getAll() {
        return repo.findAll().stream().map(e -> {
            String codiceUbicazione = magazzinoRepo.findById(e.getUbicazioneId())
                    .map(MagazzinoDigitale::getCodiceUbicazione).orElse(null);
            return toResponse(e, codiceUbicazione);
        }).toList();
    }

    public List<PosizioneArticoloResponse> getByUbicazione(Long ubicazioneId) {
        String codiceUbicazione = magazzinoRepo.findById(ubicazioneId)
                .map(MagazzinoDigitale::getCodiceUbicazione).orElse(null);
        return repo.findByUbicazioneId(ubicazioneId).stream()
                .map(e -> toResponse(e, codiceUbicazione))
                .toList();
    }

    public List<PosizioneArticoloResponse> getByArticolo(Long articoloId) {
        return repo.findByArticoloId(articoloId).stream().map(e -> {
            String codiceUbicazione = magazzinoRepo.findById(e.getUbicazioneId())
                    .map(MagazzinoDigitale::getCodiceUbicazione).orElse(null);
            return toResponse(e, codiceUbicazione);
        }).toList();
    }

    @Transactional
    public PosizioneArticoloResponse update(Long id, PosizioneArticoloRequest dto) {
        validateRequest(dto);

        MagazzinoPosizioniArticoli e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Posizione articolo non trovata con ID " + id));

        MagazzinoDigitale ubicazione = magazzinoRepo.findById(dto.ubicazioneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ubicazione non trovata con ID " + dto.ubicazioneId()));

        if (!e.getUbicazioneId().equals(dto.ubicazioneId()) || !e.getArticoloId().equals(dto.articoloId())) {
            repo.findByUbicazioneIdAndArticoloId(dto.ubicazioneId(), dto.articoloId()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DataIntegrityViolationException("Articolo " + dto.articoloId()
                            + " già presente nell'ubicazione " + dto.ubicazioneId());
                }
            });
        }

        apply(dto, e);
        return toResponse(repo.save(e), ubicazione.getCodiceUbicazione());
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Posizione articolo non trovata con ID " + id);
        }
        repo.deleteById(id);
    }

    public Integer giacenzaTotaleArticolo(Long articoloId) {
        return repo.sumQuantitaByArticoloId(articoloId);
    }

    private void validateRequest(PosizioneArticoloRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati della posizione articolo non possono essere null");
        }
        if (dto.ubicazioneId() == null) {
            throw new IllegalArgumentException("L'ID ubicazione è obbligatorio");
        }
        if (dto.articoloId() == null) {
            throw new IllegalArgumentException("L'ID articolo è obbligatorio");
        }
        if (dto.quantitaPresente() == null || dto.quantitaPresente() < 0) {
            throw new IllegalArgumentException("La quantità presente non può essere negativa o nulla");
        }
    }

    private void apply(PosizioneArticoloRequest dto, MagazzinoPosizioniArticoli e) {
        e.setUbicazioneId(dto.ubicazioneId());
        e.setArticoloId(dto.articoloId());
        e.setQuantitaPresente(dto.quantitaPresente());
    }

    private PosizioneArticoloResponse toResponse(MagazzinoPosizioniArticoli e, String codiceUbicazione) {
        return new PosizioneArticoloResponse(
                e.getId(),
                e.getUbicazioneId(),
                codiceUbicazione,
                e.getArticoloId(),
                e.getQuantitaPresente(),
                e.getAggiornatoIl()
        );
    }
}
