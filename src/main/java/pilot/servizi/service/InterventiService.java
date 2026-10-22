package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pilot.servizi.dto.request.InterventoRequest;
import pilot.servizi.dto.response.ClienteResponse;
import pilot.servizi.dto.response.InterventoResponse;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.Interventi;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.InterventiRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterventiService {

    private final InterventiRepository interventiRepo;
    private final ClientiRepository clientiRepo;

    public InterventoResponse create(InterventoRequest dto) {
        Clienti cliente = clientiRepo.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente " + dto.clienteId() + " non trovato"));

        if (dto.dataChiusura() != null && dto.dataChiusura().isBefore(dto.dataApertura())) {
            throw new IllegalArgumentException("La data di chiusura non può precedere quella di apertura");
        }

        Interventi e = new Interventi();
        apply(dto, e);
        Interventi saved = interventiRepo.save(e);
        return toResponse(saved, cliente);
    }

    public InterventoResponse getById(Long id) {
        Interventi e = interventiRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Intervento non trovato con ID " + id));
        Clienti c = clientiRepo.findById(e.getClienteId()).orElse(null);
        return toResponse(e, c);
    }

    public List<InterventoResponse> getAll() {
        return interventiRepo.findAll().stream().map(e -> {
            Clienti c = clientiRepo.findById(e.getClienteId()).orElse(null);
            return toResponse(e, c);
        }).toList();
    }

    public List<InterventoResponse> getByCliente(Long clienteId) {
        return interventiRepo.findByClienteId(clienteId).stream().map(e -> {
            Clienti c = clientiRepo.findById(e.getClienteId()).orElse(null);
            return toResponse(e, c);
        }).toList();
    }

    public InterventoResponse update(Long id, InterventoRequest dto) {
        Interventi e = interventiRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Intervento non trovato con ID " + id));
        Clienti cliente = clientiRepo.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente " + dto.clienteId() + " non trovato"));
        apply(dto, e);
        return toResponse(interventiRepo.save(e), cliente);
    }

    public void delete(Long id) {
        if (!interventiRepo.existsById(id)) {
            throw new ResourceNotFoundException("Intervento non trovato con ID " + id);
        }
        interventiRepo.deleteById(id);
    }

    private void apply(InterventoRequest dto, Interventi e) {
        e.setClienteId(dto.clienteId());
        e.setMatricolaAttrezzatura(dto.matricolaAttrezzatura());
        e.setTitolo(dto.titolo());
        e.setDescrizioneProblema(dto.descrizioneProblema());
        e.setSoluzioneApplicata(dto.soluzioneApplicata());
        e.setStato(dto.stato());
        e.setDataApertura(dto.dataApertura());
        e.setDataChiusura(dto.dataChiusura());
        e.setCostoManodopera(dto.costoManodopera());
    }

    private InterventoResponse toResponse(Interventi e, Clienti c) {
        return new InterventoResponse(
                e.getInterventoId(),                                              // 1  interventoId
                e.getClienteId(),                                                 // 2  clienteId
                c != null ? c.getRagioneSociale() : null,                         // 3  ragioneSocialeCliente
                e.getMatricolaAttrezzatura(),                                     // 4  matricolaAttrezzatura
                e.getTitolo(),                                                    // 5  titolo
                e.getDescrizioneProblema(),                                       // 6  descrizioneProblema
                e.getSoluzioneApplicata(),                                        // 7  soluzioneApplicata
                e.getStato(),                                                     // 8  stato
                e.getDataApertura(),                                              // 9  dataApertura
                e.getDataChiusura(),                                              // 10 dataChiusura
                e.getCostoManodopera()                                            // 11 costoManodopera
        );
    }
}