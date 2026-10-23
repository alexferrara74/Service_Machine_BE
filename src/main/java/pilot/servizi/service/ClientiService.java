package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.ClienteRequest;
import pilot.servizi.dto.response.ClienteResponse;
import pilot.servizi.entity.Clienti;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientiService {

    private final ClientiRepository repo;

    @Transactional
    public ClienteResponse create(ClienteRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati del cliente non possono essere null");
        }
        if (dto.codice() == null || dto.codice().trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice cliente è obbligatorio");
        }
        if (dto.ragioneSociale() == null || dto.ragioneSociale().trim().isEmpty()) {
            throw new IllegalArgumentException("La ragione sociale è obbligatoria");
        }
        if (repo.existsByCodice(dto.codice().trim())) {
            throw new DataIntegrityViolationException("Codice cliente già esistente: " + dto.codice());
        }

        Clienti e = new Clienti();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public ClienteResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente non trovato con ID " + id)));
    }

    public List<ClienteResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ClienteResponse update(Long id, ClienteRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati del cliente non possono essere null");
        }
        if (dto.codice() == null || dto.codice().trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice cliente è obbligatorio");
        }
        if (dto.ragioneSociale() == null || dto.ragioneSociale().trim().isEmpty()) {
            throw new IllegalArgumentException("La ragione sociale è obbligatoria");
        }

        Clienti e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente non trovato con ID " + id));

        // Controlla che il nuovo codice non appartenga ad un ALTRO cliente
        repo.findByCodice(dto.codice().trim()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new DataIntegrityViolationException("Codice cliente già usato da altro cliente");
            }
        });

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Cliente non trovato con ID " + id);
        }
        repo.deleteById(id);
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
