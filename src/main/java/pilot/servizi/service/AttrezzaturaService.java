package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.AttrezzaturaRequest;
import pilot.servizi.dto.response.AttrezzaturaResponse;
import pilot.servizi.entity.Attrezzature;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AttrezzatureRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttrezzaturaService {

    private final AttrezzatureRepository repo;

    @Transactional
    public AttrezzaturaResponse create(AttrezzaturaRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati dell'attrezzatura non possono essere null");
        }
        if (dto.matricola() == null || dto.matricola().trim().isEmpty()) {
            throw new IllegalArgumentException("La matricola dell'attrezzatura è obbligatoria");
        }
        if (repo.existsByMatricola(dto.matricola().trim())) {
            throw new DataIntegrityViolationException("Attrezzatura già esistente con matricola: " + dto.matricola());
        }

        Attrezzature e = new Attrezzature();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public AttrezzaturaResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attrezzatura non trovata con ID " + id)));
    }

    public AttrezzaturaResponse getByMatricola(String matricola) {
        if (matricola == null || matricola.trim().isEmpty()) {
            throw new IllegalArgumentException("La matricola per la ricerca non può essere vuota");
        }
        return toResponse(repo.findByMatricola(matricola.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Attrezzatura non trovata con matricola " + matricola)));
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
        if (dto == null) {
            throw new IllegalArgumentException("I dati dell'attrezzatura non possono essere null");
        }
        if (dto.matricola() == null || dto.matricola().trim().isEmpty()) {
            throw new IllegalArgumentException("La matricola dell'attrezzatura è obbligatoria");
        }

        Attrezzature e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attrezzatura non trovata con ID " + id));

        if (!e.getMatricola().equalsIgnoreCase(dto.matricola().trim())) {
            repo.findByMatricola(dto.matricola().trim()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new DataIntegrityViolationException("Matricola già usata da altra attrezzatura");
                }
            });
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Attrezzatura non trovata con ID " + id);
        }
        repo.deleteById(id);
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
