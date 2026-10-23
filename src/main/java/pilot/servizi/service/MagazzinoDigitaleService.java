package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.MagazzinoRequest;
import pilot.servizi.dto.response.MagazzinoResponse;
import pilot.servizi.entity.MagazzinoDigitale;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.MagazzinoDigitaleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MagazzinoDigitaleService {

    private final MagazzinoDigitaleRepository repo;

    @Transactional
    public MagazzinoResponse create(MagazzinoRequest dto) {
        validateRequest(dto);

        if (repo.existsByCodiceUbicazione(dto.codiceUbicazione().trim())) {
            throw new DataIntegrityViolationException(
                    "Ubicazione già esistente con codice: " + dto.codiceUbicazione());
        }

        MagazzinoDigitale e = new MagazzinoDigitale();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public MagazzinoResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ubicazione non trovata con ID " + id)));
    }

    public MagazzinoResponse getByCodiceUbicazione(String codiceUbicazione) {
        if (codiceUbicazione == null || codiceUbicazione.trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice ubicazione non può essere vuoto");
        }
        return toResponse(repo.findByCodiceUbicazione(codiceUbicazione.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ubicazione non trovata con codice " + codiceUbicazione)));
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
        validateRequest(dto);

        MagazzinoDigitale e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ubicazione non trovata con ID " + id));

        // Se il codice cambia, deve essere libero
        if (!e.getCodiceUbicazione().equalsIgnoreCase(dto.codiceUbicazione().trim())) {
            repo.findByCodiceUbicazione(dto.codiceUbicazione().trim())
                    .ifPresent(other -> {
                        if (!other.getUbicazioneId().equals(id)) {
                            throw new DataIntegrityViolationException(
                                    "Codice ubicazione già usato da altro record: " + dto.codiceUbicazione());
                        }
                    });
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Ubicazione non trovata con ID " + id);
        }
        repo.deleteById(id);
    }

    private void validateRequest(MagazzinoRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati dell'ubicazione non possono essere null");
        }
        if (dto.codiceUbicazione() == null || dto.codiceUbicazione().trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice ubicazione è obbligatorio");
        }
        if (dto.zona() == null || dto.zona().trim().isEmpty()) {
            throw new IllegalArgumentException("La zona è obbligatoria");
        }
        if (dto.capacitaMax() != null && dto.capacitaMax() < 1) {
            throw new IllegalArgumentException("La capacità massima deve essere almeno 1");
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
