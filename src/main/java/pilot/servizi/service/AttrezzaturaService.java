package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.AttrezzaturaRequest;
import pilot.servizi.dto.response.AttrezzaturaResponse;
import pilot.servizi.entity.Attrezzature;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.AttrezzatureRepository;
import pilot.servizi.repository.InterventiRepository;
import pilot.servizi.repository.ProdottiCatalogoRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttrezzaturaService {

    private static final Set<String> STATI = Set.of("DISPONIBILE", "IN_USO", "IN_MANUTENZIONE");

    private final AttrezzatureRepository repo;
    private final StoriaNoleggiRepository noleggiRepo;
    private final InterventiRepository interventiRepo;
    private final ProdottiCatalogoRepository prodottiRepo;

    @Transactional
    public AttrezzaturaResponse create(AttrezzaturaRequest dto) {
        validateRequest(dto, "create");
        validateArticolo(dto, null, "create");

        if (repo.existsByMatricola(dto.matricola().trim())) {
            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "create", ErrorConstants.ATTREZZATURA_MATRICOLA_DUPLICATA + dto.matricola());
            throw new DataIntegrityViolationException(ErrorConstants.ATTREZZATURA_MATRICOLA_DUPLICATA + dto.matricola());
        }

        Attrezzature e = new Attrezzature();
        e.setOrigine(Origini.MANUALE);
        apply(dto, e);
        return toResponse(repo.save(e));
    }

    public AttrezzaturaResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getById", ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                    return new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                }));
    }

    public AttrezzaturaResponse getByMatricola(String matricola) {
        if (matricola == null || matricola.trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "getByMatricola", ErrorConstants.ATTREZZATURA_MATRICOLA_OBBLIGATORIA);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_MATRICOLA_OBBLIGATORIA);
        }
        return toResponse(repo.findByMatricola(matricola.trim())
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "getByMatricola", ErrorConstants.ATTREZZATURA_NON_TROVATA_MATRICOLA + matricola);
                    return new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_MATRICOLA + matricola);
                }));
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
        validateRequest(dto, "update");
        validateArticolo(dto, id, "update");

        Attrezzature e = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "update", ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                    return new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                });

        if (!dto.matricola().trim().equalsIgnoreCase(e.getMatricola())) {
            repo.findByMatricola(dto.matricola().trim()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "update", ErrorConstants.ATTREZZATURA_MATRICOLA_DUPLICATA + dto.matricola());
                    throw new DataIntegrityViolationException(ErrorConstants.ATTREZZATURA_MATRICOLA_DUPLICATA + dto.matricola());
                }
            });
            // noleggi e interventi puntano alla matricola per valore (nessuna FK sui noleggi): cambiarla li lascerebbe orfani
            if (e.getMatricola() != null && isInUse(e.getMatricola())) {
                log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "update", ErrorConstants.ATTREZZATURA_MATRICOLA_BLOCCATA + e.getMatricola());
                throw new DataIntegrityViolationException(ErrorConstants.ATTREZZATURA_MATRICOLA_BLOCCATA + e.getMatricola());
            }
        }

        apply(dto, e);
        return toResponse(repo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        Attrezzature e = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "delete", ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                    return new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_NON_TROVATA_ID + id);
                });
        String matricola = e.getMatricola();
        if (matricola != null && isInUse(matricola)) {
            log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, "delete", ErrorConstants.ATTREZZATURA_ELIMINAZIONE_BLOCCATA + matricola);
            throw new DataIntegrityViolationException(ErrorConstants.ATTREZZATURA_ELIMINAZIONE_BLOCCATA + matricola);
        }
        repo.deleteById(id);
    }

    private boolean isInUse(String matricola) {
        return noleggiRepo.existsByMatricola(matricola) || interventiRepo.existsByMatricolaAttrezzatura(matricola);
    }

    private void validateRequest(AttrezzaturaRequest dto, String operation) {
        if (dto == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_DTO_NULL);
        }
        if (dto.matricola() == null || dto.matricola().trim().isEmpty()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_MATRICOLA_OBBLIGATORIA);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_MATRICOLA_OBBLIGATORIA);
        }
        if (dto.matricola().trim().length() > 100) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_MATRICOLA_TOO_LONG);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_MATRICOLA_TOO_LONG);
        }
        if (dto.descrizione() != null && dto.descrizione().length() > 255) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_DESCRIZIONE_TOO_LONG);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_DESCRIZIONE_TOO_LONG);
        }
        if (!STATI.contains(statoOf(dto))) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_STATO_INVALIDO);
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_STATO_INVALIDO);
        }
    }

    // L'articolo, se indicato, deve essere un'attrezzatura del catalogo e non già collegato a un'altra matricola (un articolo = una matricola).
    private void validateArticolo(AttrezzaturaRequest dto, Long id, String operation) {
        if (dto.articoloId() == null) {
            return;
        }
        var articolo = prodottiRepo.findById(dto.articoloId()).orElseThrow(() -> {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, operation, ErrorConstants.ATTREZZATURA_ARTICOLO_NON_TROVATO + dto.articoloId());
            return new ResourceNotFoundException(ErrorConstants.ATTREZZATURA_ARTICOLO_NON_TROVATO + dto.articoloId());
        });
        if (!Boolean.TRUE.equals(articolo.getIsAttrezzatura())) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.ATTREZZATURA_ARTICOLO_NON_ATTREZZATURA + dto.articoloId());
            throw new IllegalArgumentException(ErrorConstants.ATTREZZATURA_ARTICOLO_NON_ATTREZZATURA + articolo.getCodice());
        }
        repo.findByArticoloId(dto.articoloId()).ifPresent(altra -> {
            if (!altra.getId().equals(id)) {
                log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, operation, ErrorConstants.ATTREZZATURA_ARTICOLO_GIA_COLLEGATO + altra.getMatricola());
                throw new DataIntegrityViolationException(ErrorConstants.ATTREZZATURA_ARTICOLO_GIA_COLLEGATO + altra.getMatricola());
            }
        });
    }

    // Stato mancante = DISPONIBILE; altrimenti uno dei valori documentati, senza distinguere maiuscole.
    private static String statoOf(AttrezzaturaRequest dto) {
        String stato = Texts.clean(dto.stato());
        return stato == null ? "DISPONIBILE" : stato.toUpperCase(Locale.ROOT);
    }

    private void apply(AttrezzaturaRequest dto, Attrezzature e) {
        e.setMatricola(dto.matricola() != null ? dto.matricola().trim() : null);
        e.setDescrizione(Texts.clean(dto.descrizione()));
        e.setModello(Texts.clean(dto.modello()));
        e.setMarca(Texts.clean(dto.marca()));
        e.setStato(statoOf(dto));
        if (dto.articoloId() != null) {
            e.setArticoloId(dto.articoloId()); // senza articolo nella richiesta il collegamento esistente (anche quello dell'ERP) resta com'è
        }
    }

    private AttrezzaturaResponse toResponse(Attrezzature e) {
        return new AttrezzaturaResponse(
                e.getId(),
                e.getMatricola(),
                e.getDescrizione(),
                e.getModello(),
                e.getMarca(),
                e.getStato(),
                e.getOrigine(),
                e.getAggiornatoIl(),
                e.getArticoloId()
        );
    }
}
