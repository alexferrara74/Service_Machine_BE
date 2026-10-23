package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.InterventoRequest;
import pilot.servizi.dto.response.InterventoResponse;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.Interventi;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AttrezzatureRepository;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.InterventiRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterventiService {

    private final InterventiRepository interventiRepo;
    private final ClientiRepository clientiRepo;
    private final AttrezzatureRepository attrezzatureRepo;

    @Transactional
    public InterventoResponse create(InterventoRequest dto) {
        validateRequest(dto);

        Clienti cliente = clientiRepo.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente non trovato con ID " + dto.clienteId()));

        if (!attrezzatureRepo.existsByMatricola(dto.matricolaAttrezzatura().trim())) {
            throw new ResourceNotFoundException("Attrezzatura non trovata con matricola: " + dto.matricolaAttrezzatura());
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

    @Transactional
    public InterventoResponse update(Long id, InterventoRequest dto) {
        validateRequest(dto);

        Interventi e = interventiRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Intervento non trovato con ID " + id));

        Clienti cliente = clientiRepo.findById(dto.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente non trovato con ID " + dto.clienteId()));

        if (!attrezzatureRepo.existsByMatricola(dto.matricolaAttrezzatura().trim())) {
            throw new ResourceNotFoundException("Attrezzatura non trovata con matricola: " + dto.matricolaAttrezzatura());
        }

        apply(dto, e);
        return toResponse(interventiRepo.save(e), cliente);
    }

    @Transactional
    public void delete(Long id) {
        if (!interventiRepo.existsById(id)) {
            throw new ResourceNotFoundException("Intervento non trovato con ID " + id);
        }
        interventiRepo.deleteById(id);
    }

    private void validateRequest(InterventoRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati dell'intervento non possono essere null");
        }
        if (dto.clienteId() == null) {
            throw new IllegalArgumentException("L'ID cliente è obbligatorio");
        }
        if (dto.matricolaAttrezzatura() == null || dto.matricolaAttrezzatura().trim().isEmpty()) {
            throw new IllegalArgumentException("La matricola attrezzatura è obbligatoria");
        }
        if (dto.titolo() == null || dto.titolo().trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo dell'intervento è obbligatorio");
        }
        if (dto.stato() == null || dto.stato().trim().isEmpty()) {
            throw new IllegalArgumentException("Lo stato dell'intervento è obbligatorio");
        }
        if (dto.dataApertura() == null) {
            throw new IllegalArgumentException("La data di apertura è obbligatoria");
        }
        if (dto.dataChiusura() != null && dto.dataChiusura().isBefore(dto.dataApertura())) {
            throw new IllegalArgumentException("La data di chiusura non può precedere quella di apertura");
        }
        if (dto.costoManodopera() != null && dto.costoManodopera().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Il costo della manodopera non può essere negativo");
        }
    }

    private void apply(InterventoRequest dto, Interventi e) {
        e.setClienteId(dto.clienteId());
        e.setMatricolaAttrezzatura(dto.matricolaAttrezzatura().trim());
        e.setTitolo(dto.titolo().trim());
        e.setDescrizioneProblema(dto.descrizioneProblema());
        e.setSoluzioneApplicata(dto.soluzioneApplicata());
        e.setStato(dto.stato().trim());
        e.setDataApertura(dto.dataApertura());
        e.setDataChiusura(dto.dataChiusura());
        e.setCostoManodopera(dto.costoManodopera() != null ? dto.costoManodopera() : BigDecimal.ZERO);
    }

    private InterventoResponse toResponse(Interventi e, Clienti c) {
        return new InterventoResponse(
                e.getInterventoId(),
                e.getClienteId(),
                c != null ? c.getRagioneSociale() : null,
                e.getMatricolaAttrezzatura(),
                e.getTitolo(),
                e.getDescrizioneProblema(),
                e.getSoluzioneApplicata(),
                e.getStato(),
                e.getDataApertura(),
                e.getDataChiusura(),
                e.getCostoManodopera()
        );
    }
}
