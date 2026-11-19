package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.ScaffaleRequest;
import pilot.servizi.dto.response.ScaffaleLayoutResponse;
import pilot.servizi.dto.response.ScaffaleLayoutResponse.ArticoloInCella;
import pilot.servizi.dto.response.ScaffaleLayoutResponse.Cella;
import pilot.servizi.entity.MagazzinoDigitale;
import pilot.servizi.entity.MagazzinoPosizioniArticoli;
import pilot.servizi.entity.MagazzinoScaffale;
import pilot.servizi.entity.ProdottiCatalogo;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.MagazzinoDigitaleRepository;
import pilot.servizi.repository.MagazzinoPosizioniArticoliRepository;
import pilot.servizi.repository.MagazzinoScaffaleRepository;
import pilot.servizi.repository.ProdottiCatalogoRepository;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScaffaleService {

    private static final Pattern CODICE_VALIDO = Pattern.compile("^[A-Za-z0-9-]{1,20}$");
    private static final Set<Integer> ROTAZIONI = Set.of(0, 90, 180, 270);

    private final MagazzinoScaffaleRepository repo;
    private final MagazzinoDigitaleRepository ubicazioniRepo;
    private final MagazzinoPosizioniArticoliRepository posizioniRepo;
    private final ProdottiCatalogoRepository catalogoRepo;

    /** Etichetta di una cella, es. A01-R02-P03 (scaffale A01, ripiano 2 dal basso, posizione 3 da sinistra). */
    static String etichetta(String codiceScaffale, int ripiano, int posizione) {
        return String.format("%s-R%02d-P%02d", codiceScaffale, ripiano, posizione);
    }

    @Transactional
    public ScaffaleLayoutResponse create(ScaffaleRequest dto) {
        validate(dto);
        String codice = Texts.clean(dto.codice()) == null ? prossimoCodice() : dto.codice().trim().toUpperCase();
        checkCodice(codice);
        if (repo.existsByCodice(codice)) {
            throw conflict("create", ErrorConstants.SCAFFALE_CODICE_DUPLICATO + codice);
        }

        MagazzinoScaffale s = new MagazzinoScaffale();
        apply(dto, codice, s);
        s = repo.save(s);

        List<MagazzinoDigitale> celle = new ArrayList<>();
        for (int r = 1; r <= s.getNumeroRipiani(); r++) {
            for (int p = 1; p <= s.getPosizioniPerRipiano(); p++) {
                celle.add(nuovaCella(s, r, p));
            }
        }
        checkEtichetteLibere(celle, "create");
        ubicazioniRepo.saveAll(celle);
        return layoutOf(s, celle, new HashMap<>(), new HashMap<>());
    }

    @Transactional
    public ScaffaleLayoutResponse update(Long id, ScaffaleRequest dto) {
        validate(dto);
        MagazzinoScaffale s = find(id, "update");
        String codice = Texts.clean(dto.codice()) == null ? s.getCodice() : dto.codice().trim().toUpperCase();
        checkCodice(codice);
        if (repo.existsByCodiceAndScaffaleIdNot(codice, id)) {
            throw conflict("update", ErrorConstants.SCAFFALE_CODICE_DUPLICATO + codice);
        }
        apply(dto, codice, s);
        repo.save(s);

        Map<String, MagazzinoDigitale> esistenti = new HashMap<>();
        List<MagazzinoDigitale> daEliminare = new ArrayList<>();
        for (MagazzinoDigitale c : ubicazioniRepo.findByScaffaleIdOrderByRipianoNumAscPosizioneNumAsc(id)) {
            if (c.getRipianoNum() <= s.getNumeroRipiani() && c.getPosizioneNum() <= s.getPosizioniPerRipiano()) {
                esistenti.put(c.getRipianoNum() + ":" + c.getPosizioneNum(), c);
            } else {
                daEliminare.add(c);
            }
        }

        List<String> occupate = daEliminare.stream()
                .filter(c -> posizioniRepo.existsByUbicazioneId(c.getUbicazioneId()))
                .map(MagazzinoDigitale::getCodiceUbicazione).toList();
        if (!occupate.isEmpty()) {
            throw conflict("update", ErrorConstants.SCAFFALE_RIDUZIONE_BLOCCATA + String.join(", ", occupate));
        }

        List<MagazzinoDigitale> celle = new ArrayList<>();
        List<MagazzinoDigitale> nuove = new ArrayList<>();
        for (int r = 1; r <= s.getNumeroRipiani(); r++) {
            for (int p = 1; p <= s.getPosizioniPerRipiano(); p++) {
                MagazzinoDigitale c = esistenti.get(r + ":" + p);
                if (c == null) {
                    c = nuovaCella(s, r, p);
                    nuove.add(c);
                } else {
                    c.setCodiceUbicazione(etichetta(s.getCodice(), r, p));
                    c.setZona(s.getZona());
                    c.setScaffale(s.getCodice());
                }
                celle.add(c);
            }
        }
        // le etichette non devono collidere con ubicazioni di altri scaffali
        checkEtichetteLibere(celle, "update");
        ubicazioniRepo.deleteAll(daEliminare);
        ubicazioniRepo.flush();
        ubicazioniRepo.saveAll(celle);
        return layoutOf(s, celle, new HashMap<>(), new HashMap<>());
    }

    @Transactional
    public void delete(Long id) {
        MagazzinoScaffale s = find(id, "delete");
        List<MagazzinoDigitale> celle = ubicazioniRepo.findByScaffaleIdOrderByRipianoNumAscPosizioneNumAsc(id);
        List<String> occupate = celle.stream()
                .filter(c -> posizioniRepo.existsByUbicazioneId(c.getUbicazioneId()))
                .map(MagazzinoDigitale::getCodiceUbicazione).toList();
        if (!occupate.isEmpty()) {
            throw conflict("delete", ErrorConstants.SCAFFALE_ELIMINAZIONE_BLOCCATA + String.join(", ", occupate));
        }
        ubicazioniRepo.deleteAll(celle);
        ubicazioniRepo.flush();
        repo.delete(s);
    }

    public ScaffaleLayoutResponse getById(Long id) {
        MagazzinoScaffale s = find(id, "getById");
        List<MagazzinoDigitale> celle = ubicazioniRepo.findByScaffaleIdOrderByRipianoNumAscPosizioneNumAsc(id);
        Set<Long> ids = celle.stream().map(MagazzinoDigitale::getUbicazioneId).collect(Collectors.toSet());
        List<MagazzinoPosizioniArticoli> pos = posizioniRepo.findAll().stream()
                .filter(p -> ids.contains(p.getUbicazioneId())).toList();
        return layoutOf(s, celle, byUbicazione(pos), catalogo(pos));
    }

    /** Tutti gli scaffali con celle e contenuto: è il payload della vista 3D. */
    public List<ScaffaleLayoutResponse> layout() {
        List<MagazzinoScaffale> scaffali = repo.findAllByOrderByCodiceAsc();
        Map<Long, List<MagazzinoDigitale>> celle = ubicazioniRepo.findByScaffaleIdIsNotNull().stream()
                .sorted(Comparator.comparing(MagazzinoDigitale::getRipianoNum)
                        .thenComparing(MagazzinoDigitale::getPosizioneNum))
                .collect(Collectors.groupingBy(MagazzinoDigitale::getScaffaleId));
        Set<Long> ids = celle.values().stream().flatMap(List::stream)
                .map(MagazzinoDigitale::getUbicazioneId).collect(Collectors.toSet());
        List<MagazzinoPosizioniArticoli> pos = posizioniRepo.findAll().stream()
                .filter(p -> ids.contains(p.getUbicazioneId())).toList();
        Map<Long, List<MagazzinoPosizioniArticoli>> perCella = byUbicazione(pos);
        Map<Long, ProdottiCatalogo> prodotti = catalogo(pos);
        return scaffali.stream()
                .map(s -> layoutOf(s, celle.getOrDefault(s.getScaffaleId(), List.of()), perCella, prodotti))
                .toList();
    }

    // ---------------------------------------------------------------------------------------

    private Map<Long, List<MagazzinoPosizioniArticoli>> byUbicazione(List<MagazzinoPosizioniArticoli> pos) {
        return pos.stream().collect(Collectors.groupingBy(MagazzinoPosizioniArticoli::getUbicazioneId));
    }

    private Map<Long, ProdottiCatalogo> catalogo(List<MagazzinoPosizioniArticoli> pos) {
        Set<Long> articoli = pos.stream().map(MagazzinoPosizioniArticoli::getArticoloId).collect(Collectors.toSet());
        return catalogoRepo.findAllById(articoli).stream()
                .collect(Collectors.toMap(ProdottiCatalogo::getArticoloId, Function.identity()));
    }

    private ScaffaleLayoutResponse layoutOf(MagazzinoScaffale s, List<MagazzinoDigitale> celle,
                                            Map<Long, List<MagazzinoPosizioniArticoli>> perCella,
                                            Map<Long, ProdottiCatalogo> prodotti) {
        List<Cella> out = celle.stream().map(c -> new Cella(
                c.getUbicazioneId(),
                c.getCodiceUbicazione(),
                c.getRipianoNum(),
                c.getPosizioneNum(),
                perCella.getOrDefault(c.getUbicazioneId(), List.of()).stream().map(p -> {
                    ProdottiCatalogo prod = prodotti.get(p.getArticoloId());
                    return new ArticoloInCella(p.getId(), p.getArticoloId(),
                            prod == null ? null : prod.getCodice(),
                            prod == null ? null : prod.getDescrizione(),
                            p.getQuantitaPresente());
                }).toList()
        )).toList();
        return new ScaffaleLayoutResponse(s.getScaffaleId(), s.getCodice(), s.getZona(), s.getLarghezzaCm(),
                s.getProfonditaCm(), s.getAltezzaCm(), s.getPosXCm(), s.getPosYCm(), s.getRotazioneGradi(),
                s.getNumeroRipiani(), s.getPosizioniPerRipiano(), out);
    }

    private MagazzinoDigitale nuovaCella(MagazzinoScaffale s, int ripiano, int posizione) {
        MagazzinoDigitale c = new MagazzinoDigitale();
        c.setCodiceUbicazione(etichetta(s.getCodice(), ripiano, posizione));
        c.setZona(s.getZona());
        c.setScaffale(s.getCodice());
        c.setRipiano(String.valueOf(ripiano));
        c.setScaffaleId(s.getScaffaleId());
        c.setRipianoNum(ripiano);
        c.setPosizioneNum(posizione);
        return c;
    }

    private void checkEtichetteLibere(List<MagazzinoDigitale> celle, String op) {
        for (MagazzinoDigitale c : celle) {
            Optional<MagazzinoDigitale> altra = ubicazioniRepo.findByCodiceUbicazione(c.getCodiceUbicazione());
            if (altra.isPresent() && !Objects.equals(altra.get().getUbicazioneId(), c.getUbicazioneId())) {
                throw conflict(op, ErrorConstants.MAGAZZINO_CODICE_DUPLICATO + c.getCodiceUbicazione());
            }
        }
    }

    private String prossimoCodice() {
        int n = (int) repo.count() + 1;
        String codice = String.format("S%02d", n);
        while (repo.existsByCodice(codice)) {
            codice = String.format("S%02d", ++n);
        }
        return codice;
    }

    private void checkCodice(String codice) {
        if (!CODICE_VALIDO.matcher(codice).matches()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "codice", ErrorConstants.SCAFFALE_CODICE_NON_VALIDO);
            throw new IllegalArgumentException(ErrorConstants.SCAFFALE_CODICE_NON_VALIDO);
        }
    }

    private void validate(ScaffaleRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException(ErrorConstants.SCAFFALE_DTO_NULL);
        }
        if (dto.zona() == null || dto.zona().isBlank()) {
            throw new IllegalArgumentException(ErrorConstants.MAGAZZINO_ZONA_OBBLIGATORIA);
        }
        if (dto.larghezzaCm() == null || dto.profonditaCm() == null || dto.altezzaCm() == null
                || dto.larghezzaCm() < 1 || dto.profonditaCm() < 1 || dto.altezzaCm() < 1) {
            throw new IllegalArgumentException(ErrorConstants.SCAFFALE_DIMENSIONI_INVALIDE);
        }
        if (dto.rotazioneGradi() == null || !ROTAZIONI.contains(dto.rotazioneGradi())) {
            throw new IllegalArgumentException(ErrorConstants.SCAFFALE_ROTAZIONE_INVALIDA);
        }
        if (dto.numeroRipiani() == null || dto.posizioniPerRipiano() == null
                || dto.numeroRipiani() < 1 || dto.numeroRipiani() > 50
                || dto.posizioniPerRipiano() < 1 || dto.posizioniPerRipiano() > 50) {
            throw new IllegalArgumentException(ErrorConstants.SCAFFALE_GRIGLIA_INVALIDA);
        }
    }

    private void apply(ScaffaleRequest dto, String codice, MagazzinoScaffale s) {
        s.setCodice(codice);
        s.setZona(dto.zona().trim());
        s.setLarghezzaCm(dto.larghezzaCm());
        s.setProfonditaCm(dto.profonditaCm());
        s.setAltezzaCm(dto.altezzaCm());
        s.setPosXCm(dto.posXCm() == null ? 0 : dto.posXCm());
        s.setPosYCm(dto.posYCm() == null ? 0 : dto.posYCm());
        s.setRotazioneGradi(dto.rotazioneGradi());
        s.setNumeroRipiani(dto.numeroRipiani());
        s.setPosizioniPerRipiano(dto.posizioniPerRipiano());
    }

    private MagazzinoScaffale find(Long id, String op) {
        return repo.findById(id).orElseThrow(() -> {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, op, ErrorConstants.SCAFFALE_NON_TROVATO + id);
            return new ResourceNotFoundException(ErrorConstants.SCAFFALE_NON_TROVATO + id);
        });
    }

    private DataIntegrityViolationException conflict(String op, String msg) {
        log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, op, msg);
        return new DataIntegrityViolationException(msg);
    }
}
