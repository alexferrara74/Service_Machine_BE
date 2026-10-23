package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.InterventoRequest;
import pilot.servizi.dto.response.InterventoResponse;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.Interventi;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AttrezzatureRepository;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.InterventiRepository;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterventiService {

    private final InterventiRepository interventiRepo;
    private final ClientiRepository clientiRepo;
    private final AttrezzatureRepository attrezzatureRepo;

    @Transactional
    public InterventoResponse create(InterventoRequest dto) {
        validateRequest(dto, "create");

        Clienti cliente = clientiRepo.findById(dto.clienteId())
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "create", ErrorConstants.CLIENTE_NON_TROVATO + dto.clienteId());
                    return new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + dto.clienteId());
                });

        if (!attrezzatureRepo.existsByMatricola(dto.matricolaAttrezzatura().trim())) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "create", ErrorConstants.ATTREZZATURA_NON_TROVATA_MATRICOLA + dto.matricolaAttrezzatura());
            throw new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_MATRICOLA + dto.matricolaAttrezzatura());
        }

        Interventi e = new Interventi();
        apply(dto, e);
        Interventi saved = interventiRepo.save(e);
        return toResponse(saved, cliente);
    }

    public InterventoResponse getById(Long id) {
        Interventi e = interventiRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getById", ErrorConstants.INTERVENTO_NON_TROVATO + id);
                    return new ResourceNotFoundException(ErrorConstants.INTERVENTO_NON_TROVATO + id);
                });
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
        validateRequest(dto, "update");

        Interventi e = interventiRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.INTERVENTO_NON_TROVATO + id);
                    return new ResourceNotFoundException(ErrorConstants.INTERVENTO_NON_TROVATO + id);
                });

        Clienti cliente = clientiRepo.findById(dto.clienteId())
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.CLIENTE_NON_TROVATO + dto.clienteId());
                    return new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + dto.clienteId());
                });

        if (!attrezzatureRepo.existsByMatricola(dto.matricolaAttrezzatura().trim())) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.ATTREZZATURA_NON_TROVATA_MATRICOLA + dto.matricolaAttrezzatura());
            throw new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_MATRICOLA + dto.matricolaAttrezzatura());
        }

        apply(dto, e);
        return toResponse(interventiRepo.save(e), cliente);
    }

    @Transactional
    public void delete(Long id) {
        if (!interventiRepo.existsById(id)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "delete", ErrorConstants.INTERVENTO_NON_TROVATO + id);
            throw new ResourceNotFoundException(ErrorConstants.INTERVENTO_NON_TROVATO + id);
        }
        interventiRepo.deleteById(id);
    }

    private void validateRequest(InterventoRequest dto, String operation) {
        if (dto == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.INTERVENTO_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.INTERVENTO_DTO_NULL);
        }
        if (dto.clienteId() == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.INTERVENTO_CLIENTE_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.INTERVENTO_CLIENTE_OBBLIGATORIO);
        }
        if (dto.matricolaAttrezzatura() == null || dto.matricolaAttrezzatura().trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.INTERVENTO_MATRICOLA_OBBLIGATORIA);
            throw new IllegalArgumentException(ErrorConstants.INTERVENTO_MATRICOLA_OBBLIGATORIA);
        }
        if (dto.titolo() == null || dto.titolo().trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.INTERVENTO_TITOLO_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.INTERVENTO_TITOLO_OBBLIGATORIO);
        }
        if (dto.stato() == null || dto.stato().trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.INTERVENTO_STATO_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.INTERVENTO_STATO_OBBLIGATORIO);
        }
        if (dto.dataApertura() == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.INTERVENTO_DATA_APERTURA_OBBLIGATORIA);
            throw new IllegalArgumentException(ErrorConstants.INTERVENTO_DATA_APERTURA_OBBLIGATORIA);
        }
        if (dto.dataChiusura() != null && dto.dataChiusura().isBefore(dto.dataApertura())) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.INTERVENTO_DATE_INCOERENTI);
            throw new IllegalArgumentException(ErrorConstants.INTERVENTO_DATE_INCOERENTI);
        }
        if (dto.costoManodopera() != null && dto.costoManodopera().compareTo(BigDecimal.ZERO) < 0) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.INTERVENTO_COSTO_NEGATIVO);
            throw new IllegalArgumentException(ErrorConstants.INTERVENTO_COSTO_NEGATIVO);
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
