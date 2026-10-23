package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.StoriaProdottoRequest;
import pilot.servizi.dto.response.StoriaProdottoResponse;
import pilot.servizi.entity.StoriaProdotti;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.StoriaProdottiRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoriaProdottiService {

    private final StoriaProdottiRepository repo;
    private final ClientiRepository clientiRepo;

    @Transactional
    public StoriaProdottoResponse create(StoriaProdottoRequest dto) {
        validateRequest(dto);

        if (dto.clienteId() != null && !clientiRepo.existsById(dto.clienteId())) {
            throw new ResourceNotFoundException("Cliente non trovato con ID " + dto.clienteId());
        }

        StoriaProdotti e = new StoriaProdotti();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public StoriaProdottoResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record storico prodotti non trovato con ID " + id)));
    }

    public List<StoriaProdottoResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public List<StoriaProdottoResponse> getByCliente(Long clienteId) {
        return repo.findByClienteId(clienteId).stream().map(this::toResponse).toList();
    }

    public List<StoriaProdottoResponse> getByArticolo(Long articoloId) {
        return repo.findByArticoloId(articoloId).stream().map(this::toResponse).toList();
    }

    public List<StoriaProdottoResponse> getByCodiceArticolo(String codiceArticolo) {
        return repo.findByCodiceArticolo(codiceArticolo).stream().map(this::toResponse).toList();
    }

    @Transactional
    public StoriaProdottoResponse update(Long id, StoriaProdottoRequest dto) {
        validateRequest(dto);

        StoriaProdotti e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record storico prodotti non trovato con ID " + id));

        if (dto.clienteId() != null && !clientiRepo.existsById(dto.clienteId())) {
            throw new ResourceNotFoundException("Cliente non trovato con ID " + dto.clienteId());
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Record storico prodotti non trovato con ID " + id);
        }
        repo.deleteById(id);
    }

    private void validateRequest(StoriaProdottoRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati del prodotto non possono essere null");
        }
        if (dto.codiceArticolo() == null || dto.codiceArticolo().trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice articolo è obbligatorio");
        }
        if (dto.quantita() == null || dto.quantita() < 0) {
            throw new IllegalArgumentException("La quantità è obbligatoria e non può essere negativa");
        }
    }

    private void apply(StoriaProdottoRequest dto, StoriaProdotti e) {
        e.setClienteId(dto.clienteId());
        e.setArticoloId(dto.articoloId());
        e.setCodiceArticolo(dto.codiceArticolo().trim());
        e.setDescrizione(dto.descrizione());
        e.setCategoriaId(dto.categoriaId());
        e.setCategoriaDescrizione(dto.categoriaDescrizione());
        e.setQuantita(dto.quantita());
        e.setDataDoc(dto.dataDoc());
    }

    private StoriaProdottoResponse toResponse(StoriaProdotti e) {
        return new StoriaProdottoResponse(
                e.getId(),
                e.getClienteId(),
                e.getArticoloId(),
                e.getCodiceArticolo(),
                e.getDescrizione(),
                e.getCategoriaId(),
                e.getCategoriaDescrizione(),
                e.getQuantita(),
                e.getDataDoc(),
                e.getAggiornatoIl()
        );
    }
}
