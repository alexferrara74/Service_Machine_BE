package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.PosizioneArticoloRequest;
import pilot.servizi.dto.response.PosizioneArticoloResponse;
import pilot.servizi.entity.MagazzinoDigitale;
import pilot.servizi.entity.MagazzinoPosizioniArticoli;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.MagazzinoDigitaleRepository;
import pilot.servizi.repository.MagazzinoPosizioniArticoliRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MagazzinoPosizioniArticoliService {

    private final MagazzinoPosizioniArticoliRepository repo;
    private final MagazzinoDigitaleRepository magazzinoRepo;

    @Transactional
    public PosizioneArticoloResponse create(PosizioneArticoloRequest dto) {
        validateRequest(dto, "create");

        MagazzinoDigitale ubicazione = magazzinoRepo.findById(dto.ubicazioneId())
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "create", ErrorConstants.MAGAZZINO_NON_TROVATO + dto.ubicazioneId());
                    return new ResourceNotFoundException(ErrorConstants.MAGAZZINO_NON_TROVATO + dto.ubicazioneId());
                });

        if (repo.existsByUbicazioneIdAndArticoloId(dto.ubicazioneId(), dto.articoloId())) {
            String msg = ErrorConstants.POSIZIONE_DUPLICATA + "Articolo " + dto.articoloId() + " in Ubicazione " + dto.ubicazioneId();
            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "create", msg);
            throw new DataIntegrityViolationException(msg);
        }

        MagazzinoPosizioniArticoli e = new MagazzinoPosizioniArticoli();
        apply(dto, e);
        return toResponse(repo.save(e), ubicazione.getCodiceUbicazione());
    }

    public PosizioneArticoloResponse getById(Long id) {
        MagazzinoPosizioniArticoli e = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getById", ErrorConstants.POSIZIONE_NON_TROVATA + id);
                    return new ResourceNotFoundException(ErrorConstants.POSIZIONE_NON_TROVATA + id);
                });
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
        validateRequest(dto, "update");

        MagazzinoPosizioniArticoli e = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.POSIZIONE_NON_TROVATA + id);
                    return new ResourceNotFoundException(ErrorConstants.POSIZIONE_NON_TROVATA + id);
                });

        MagazzinoDigitale ubicazione = magazzinoRepo.findById(dto.ubicazioneId())
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.MAGAZZINO_NON_TROVATO + dto.ubicazioneId());
                    return new ResourceNotFoundException(ErrorConstants.MAGAZZINO_NON_TROVATO + dto.ubicazioneId());
                });

        if (!e.getUbicazioneId().equals(dto.ubicazioneId()) || !e.getArticoloId().equals(dto.articoloId())) {
            repo.findByUbicazioneIdAndArticoloId(dto.ubicazioneId(), dto.articoloId()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    String msg = ErrorConstants.POSIZIONE_DUPLICATA + "Articolo " + dto.articoloId() + " in Ubicazione " + dto.ubicazioneId();
                    log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "update", msg);
                    throw new DataIntegrityViolationException(msg);
                }
            });
        }

        apply(dto, e);
        return toResponse(repo.save(e), ubicazione.getCodiceUbicazione());
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "delete", ErrorConstants.POSIZIONE_NON_TROVATA + id);
            throw new ResourceNotFoundException(ErrorConstants.POSIZIONE_NON_TROVATA + id);
        }
        repo.deleteById(id);
    }

    public Integer giacenzaTotaleArticolo(Long articoloId) {
        return repo.sumQuantitaByArticoloId(articoloId);
    }

    private void validateRequest(PosizioneArticoloRequest dto, String operation) {
        if (dto == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.POSIZIONE_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.POSIZIONE_DTO_NULL);
        }
        if (dto.ubicazioneId() == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.POSIZIONE_UBICAZIONE_OBBLIGATORIA);
            throw new IllegalArgumentException(ErrorConstants.POSIZIONE_UBICAZIONE_OBBLIGATORIA);
        }
        if (dto.articoloId() == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.POSIZIONE_ARTICOLO_OBBLIGATORIO);
            throw new IllegalArgumentException(ErrorConstants.POSIZIONE_ARTICOLO_OBBLIGATORIO);
        }
        if (dto.quantitaPresente() == null || dto.quantitaPresente() < 0) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.POSIZIONE_QUANTITA_INVALIDA);
            throw new IllegalArgumentException(ErrorConstants.POSIZIONE_QUANTITA_INVALIDA);
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
