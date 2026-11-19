package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pilot.servizi.dto.request.AppuntamentoRequest;
import pilot.servizi.dto.response.AppuntamentoResponse;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.NoleggioAppuntamento;
import pilot.servizi.entity.NoleggioAppuntamento.Esito;
import pilot.servizi.entity.NoleggioAppuntamento.Stato;
import pilot.servizi.entity.NoleggioAppuntamentoFoto;
import pilot.servizi.entity.NoleggioManutenzione;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.NoleggioAppuntamentoFotoRepository;
import pilot.servizi.repository.NoleggioAppuntamentoRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoleggioAppuntamentoService {

    static final long MAX_FOTO_BYTES = 10L * 1024 * 1024;
    static final int MAX_FOTO = 20;

    public record FotoFile(String nomeFile, String contentType, byte[] contenuto) {}

    private final NoleggioAppuntamentoRepository repo;
    private final NoleggioAppuntamentoFotoRepository fotoRepo;
    private final StoriaNoleggiRepository noleggiRepo;
    private final ClientiRepository clientiRepo;
    private final NoleggioManutenzioneService manutenzioneService;

    public List<AppuntamentoResponse> list(Integer docId) {
        getNoleggio(docId);
        return repo.findByDocIdOrderByDataPrevistaDesc(docId).stream().map(this::toResponse).toList();
    }

    /** Agenda globale; con stato nullo restituisce tutti gli appuntamenti. */
    public List<AppuntamentoResponse> listAll(String stato) {
        List<NoleggioAppuntamento> items = stato == null || stato.isBlank()
                ? repo.findAllByOrderByDataPrevistaDesc()
                : repo.findByStatoOrderByDataPrevistaAsc(parse(Stato.class, stato, "Stato non valido: " + stato));
        return items.stream().map(this::toResponse).toList();
    }

    public AppuntamentoResponse get(Integer docId, Long id) {
        return toResponse(find(docId, id));
    }

    @Transactional
    public AppuntamentoResponse create(Integer docId, AppuntamentoRequest dto) {
        getNoleggio(docId);
        NoleggioAppuntamento a = NoleggioAppuntamento.builder()
                .docId(docId)
                .stato(Stato.PROGRAMMATO)
                .build();
        applyBozza(dto, a);
        return toResponse(repo.save(a));
    }

    /** Salva i dati dell'appuntamento ancora aperto (anche come bozza del resoconto). */
    @Transactional
    public AppuntamentoResponse update(Integer docId, Long id, AppuntamentoRequest dto) {
        NoleggioAppuntamento a = find(docId, id);
        requireAperto(a, "update");
        applyBozza(dto, a);
        return toResponse(repo.save(a));
    }

    /**
     * Chiude l'appuntamento e registra la manutenzione nello storico del noleggio con il tipo scelto
     * dall'operatore. Con esito positivo la prossima scadenza viene allungata di giorniProroga
     * (se non indicato: metà dell'intervallo attivo).
     */
    @Transactional
    public AppuntamentoResponse completa(Integer docId, Long id, AppuntamentoRequest dto) {
        NoleggioAppuntamento a = find(docId, id);
        requireAperto(a, "completa");
        applyBozza(dto, a);
        if (a.getEsito() == null) {
            throw validation(ErrorConstants.APPUNTAMENTO_ESITO_OBBLIGATORIO);
        }
        if (a.getResoconto() == null) {
            throw validation(ErrorConstants.APPUNTAMENTO_RESOCONTO_OBBLIGATORIO);
        }

        int proroga = 0;
        if (a.getEsito() == Esito.POSITIVO) {
            proroga = a.getGiorniProroga() != null ? a.getGiorniProroga() : propostaProroga(docId);
        }
        a.setGiorniProroga(proroga);

        NoleggioManutenzione evento = manutenzioneService.registraDaAppuntamento(
                docId, a.getTipo(), a.getResoconto(), a.getEsito().name(), proroga, a.getId());
        a.setStato(Stato.COMPLETATO);
        a.setCompletatoIl(evento.getEseguitaIl());
        a.setManutenzioneId(evento.getId());
        return toResponse(repo.save(a));
    }

    @Transactional
    public AppuntamentoResponse annulla(Integer docId, Long id) {
        NoleggioAppuntamento a = find(docId, id);
        requireAperto(a, "annulla");
        a.setStato(Stato.ANNULLATO);
        return toResponse(repo.save(a));
    }

    @Transactional
    public void delete(Integer docId, Long id) {
        NoleggioAppuntamento a = find(docId, id);
        if (a.getStato() == Stato.COMPLETATO) {
            throw conflict("delete", ErrorConstants.APPUNTAMENTO_ELIMINAZIONE_BLOCCATA + id);
        }
        repo.delete(a);
    }

    // ---- foto -----------------------------------------------------------------------------

    @Transactional
    public AppuntamentoResponse.Foto aggiungiFoto(Integer docId, Long id, MultipartFile file) {
        NoleggioAppuntamento a = find(docId, id);
        if (a.getStato() == Stato.ANNULLATO) {
            throw conflict("foto", ErrorConstants.APPUNTAMENTO_NON_MODIFICABILE + id);
        }
        if (file == null || file.isEmpty()) {
            throw validation(ErrorConstants.FOTO_NON_VALIDA);
        }
        if (file.getSize() > MAX_FOTO_BYTES) {
            throw validation(ErrorConstants.FOTO_TROPPO_GRANDE);
        }
        if (fotoRepo.countByAppuntamentoId(id) >= MAX_FOTO) {
            throw conflict("foto", ErrorConstants.FOTO_LIMITE);
        }
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile leggere la foto caricata.", e);
        }
        String tipo = riconosciImmagine(content);
        if (tipo == null) {
            throw validation(ErrorConstants.FOTO_NON_VALIDA);
        }
        NoleggioAppuntamentoFoto foto = new NoleggioAppuntamentoFoto();
        foto.setAppuntamentoId(id);
        foto.setNomeFile(nomeSicuro(file.getOriginalFilename(), tipo));
        foto.setContentType(tipo);
        foto.setContenuto(content);
        foto = fotoRepo.save(foto);
        return new AppuntamentoResponse.Foto(foto.getId(), foto.getNomeFile());
    }

    public FotoFile scaricaFoto(Integer docId, Long id, Long fotoId) {
        find(docId, id);
        NoleggioAppuntamentoFoto f = fotoRepo.findByIdAndAppuntamentoId(fotoId, id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.FOTO_NON_TROVATA + fotoId));
        return new FotoFile(f.getNomeFile(), f.getContentType(), f.getContenuto());
    }

    @Transactional
    public void eliminaFoto(Integer docId, Long id, Long fotoId) {
        NoleggioAppuntamento a = find(docId, id);
        if (a.getStato() == Stato.ANNULLATO) {
            throw conflict("foto", ErrorConstants.APPUNTAMENTO_NON_MODIFICABILE + id);
        }
        NoleggioAppuntamentoFoto f = fotoRepo.findByIdAndAppuntamentoId(fotoId, id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.FOTO_NON_TROVATA + fotoId));
        fotoRepo.delete(f);
    }

    /** Solo JPEG, PNG e WebP, riconosciuti dai primi byte e non dal nome del file. */
    static String riconosciImmagine(byte[] c) {
        if (c.length > 3 && (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (c.length > 8 && (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G') {
            return "image/png";
        }
        if (c.length > 12 && c[0] == 'R' && c[1] == 'I' && c[2] == 'F' && c[3] == 'F'
                && c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    // ---- interni --------------------------------------------------------------------------

    private int propostaProroga(Integer docId) {
        try {
            Integer intervallo = manutenzioneService.getStato(docId).intervalloManutenzioneGiorni();
            return intervallo == null ? 0 : intervallo / 2;
        } catch (RuntimeException e) {
            return 0; // noleggio senza cliente o senza piano: nessuna proposta
        }
    }

    private void applyBozza(AppuntamentoRequest dto, NoleggioAppuntamento a) {
        if (dto == null) {
            throw validation("I dati dell'appuntamento non possono essere null");
        }
        a.setDataPrevista(dto.dataPrevista());
        a.setTipo(parse(NoleggioManutenzione.Tipo.class, dto.tipo(), ErrorConstants.APPUNTAMENTO_TIPO_INVALIDO));
        a.setEsito(dto.esito() == null || dto.esito().isBlank()
                ? null : parse(Esito.class, dto.esito(), ErrorConstants.APPUNTAMENTO_ESITO_INVALIDO));
        a.setResoconto(Texts.clean(dto.resoconto()));
        a.setGiorniProroga(dto.giorniProroga());
    }

    private <E extends Enum<E>> E parse(Class<E> type, String value, String message) {
        try {
            return Enum.valueOf(type, value == null ? "" : value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw validation(message);
        }
    }

    private void requireAperto(NoleggioAppuntamento a, String op) {
        if (a.getStato() != Stato.PROGRAMMATO) {
            throw conflict(op, ErrorConstants.APPUNTAMENTO_NON_MODIFICABILE + a.getId());
        }
    }

    private String nomeSicuro(String originale, String tipo) {
        String estensione = switch (tipo) { case "image/png" -> ".png"; case "image/webp" -> ".webp"; default -> ".jpg"; };
        String base = originale == null ? "foto" : originale.replaceAll("^.*[\\\\/]", "").replaceAll("\\.[^.]*$", "");
        base = base.replaceAll("[^A-Za-z0-9_-]", "_");
        if (base.isBlank()) base = "foto";
        if (base.length() > 80) base = base.substring(0, 80);
        return base + estensione;
    }

    private StoriaNoleggi getNoleggio(Integer docId) {
        return noleggiRepo.findById(docId).orElseThrow(() -> {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "appuntamenti", ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
            return new ResourceNotFoundException(ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
        });
    }

    private NoleggioAppuntamento find(Integer docId, Long id) {
        getNoleggio(docId);
        return repo.findByIdAndDocId(id, docId).orElseThrow(() -> {
            log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, "appuntamenti", ErrorConstants.APPUNTAMENTO_NON_TROVATO + id);
            return new ResourceNotFoundException(ErrorConstants.APPUNTAMENTO_NON_TROVATO + id);
        });
    }

    private IllegalArgumentException validation(String msg) {
        log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "appuntamenti", msg);
        return new IllegalArgumentException(msg);
    }

    private DataIntegrityViolationException conflict(String op, String msg) {
        log.warn(ErrorConstants.LOG_CONFLICT, ErrorConstants.ERR_CONFLICT, op, msg);
        return new DataIntegrityViolationException(msg);
    }

    private AppuntamentoResponse toResponse(NoleggioAppuntamento a) {
        StoriaNoleggi n = noleggiRepo.findById(a.getDocId()).orElse(null);
        Long clienteId = n == null ? null : n.getClienteId();
        String cliente = null;
        if (clienteId != null) {
            cliente = clientiRepo.findById(clienteId).map(this::nomeCliente).orElse(null);
        }
        Integer intervallo = null;
        if (a.getStato() == Stato.PROGRAMMATO) {
            try {
                intervallo = manutenzioneService.getStato(a.getDocId()).intervalloManutenzioneGiorni();
            } catch (RuntimeException ignored) {
                // noleggio senza cliente o piano: l'intervallo resta sconosciuto
            }
        }
        List<AppuntamentoResponse.Foto> foto = fotoRepo.findByAppuntamentoIdOrderByIdAsc(a.getId()).stream()
                .map(f -> new AppuntamentoResponse.Foto(f.getId(), f.getNomeFile())).toList();
        return new AppuntamentoResponse(a.getId(), a.getDocId(), clienteId, cliente,
                n == null ? null : n.getMatricola(), a.getDataPrevista(), a.getStato().name(), a.getTipo().name(),
                a.getEsito() == null ? null : a.getEsito().name(), a.getResoconto(), a.getGiorniProroga(),
                intervallo, a.getCompletatoIl(), foto);
    }

    private String nomeCliente(Clienti c) {
        if (c.getRagioneSociale() != null && !c.getRagioneSociale().isBlank()) return c.getRagioneSociale();
        return ((c.getNome() == null ? "" : c.getNome()) + " " + (c.getCognome() == null ? "" : c.getCognome())).trim();
    }
}
