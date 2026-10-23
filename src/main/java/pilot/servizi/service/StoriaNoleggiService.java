package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.StoriaNoleggioRequest;
import pilot.servizi.dto.response.StoriaNoleggioResponse;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoriaNoleggiService {

    private final StoriaNoleggiRepository repo;
    private final ClientiRepository clientiRepo;

    @Transactional
    public StoriaNoleggioResponse create(StoriaNoleggioRequest dto) {
        validateRequest(dto);

        if (repo.existsById(dto.docId())) {
            throw new DataIntegrityViolationException("Documento di noleggio già esistente con ID " + dto.docId());
        }

        if (dto.clienteId() != null && !clientiRepo.existsById(dto.clienteId())) {
            throw new ResourceNotFoundException("Cliente non trovato con ID " + dto.clienteId());
        }

        StoriaNoleggi e = new StoriaNoleggi();
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public StoriaNoleggioResponse getById(Integer docId) {
        return toResponse(repo.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento noleggio non trovato con ID " + docId)));
    }

    public List<StoriaNoleggioResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public List<StoriaNoleggioResponse> getByCliente(Long clienteId) {
        return repo.findByClienteId(clienteId).stream().map(this::toResponse).toList();
    }

    public List<StoriaNoleggioResponse> getByArticolo(Long articoloId) {
        return repo.findByArticoloId(articoloId).stream().map(this::toResponse).toList();
    }

    public List<StoriaNoleggioResponse> getByMatricola(String matricola) {
        return repo.findByMatricola(matricola).stream().map(this::toResponse).toList();
    }

    @Transactional
    public StoriaNoleggioResponse update(Integer docId, StoriaNoleggioRequest dto) {
        validateRequest(dto);

        StoriaNoleggi e = repo.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento noleggio non trovato con ID " + docId));

        if (dto.clienteId() != null && !clientiRepo.existsById(dto.clienteId())) {
            throw new ResourceNotFoundException("Cliente non trovato con ID " + dto.clienteId());
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Integer docId) {
        if (!repo.existsById(docId)) {
            throw new ResourceNotFoundException("Documento noleggio non trovato con ID " + docId);
        }
        repo.deleteById(docId);
    }

    private void validateRequest(StoriaNoleggioRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati del noleggio non possono essere null");
        }
        if (dto.docId() == null) {
            throw new IllegalArgumentException("L'ID documento è obbligatorio");
        }
        if (dto.quantita() != null && dto.quantita() < 0) {
            throw new IllegalArgumentException("La quantità non può essere negativa");
        }
    }

    private void apply(StoriaNoleggioRequest dto, StoriaNoleggi e) {
        e.setDocId(dto.docId());
        e.setSezionale(dto.sezionale());
        e.setDataDoc(dto.dataDoc());
        e.setClienteId(dto.clienteId());
        e.setArticoloId(dto.articoloId());
        e.setMatricola(dto.matricola());
        e.setQuantita(dto.quantita());
    }

    private StoriaNoleggioResponse toResponse(StoriaNoleggi e) {
        return new StoriaNoleggioResponse(
                e.getDocId(),
                e.getSezionale(),
                e.getDataDoc(),
                e.getClienteId(),
                e.getArticoloId(),
                e.getMatricola(),
                e.getQuantita(),
                e.getAggiornatoIl()
        );
    }
}
