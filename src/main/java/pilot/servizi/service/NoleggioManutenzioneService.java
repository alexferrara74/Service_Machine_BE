package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.request.NoleggioManutenzioneConfigurazioneRequest;
import pilot.servizi.dto.request.NoleggioManutenzionePianificazioneRequest;
import pilot.servizi.dto.request.NoleggioManutenzioneRequest;
import pilot.servizi.dto.response.ClienteManutenzionePianificazioneResponse;
import pilot.servizi.dto.response.NoleggioManutenzioneEventoResponse;
import pilot.servizi.dto.response.NoleggioManutenzionePeriodoResponse;
import pilot.servizi.dto.response.NoleggioManutenzioneResponse;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.ClienteManutenzionePeriodo;
import pilot.servizi.entity.ClienteManutenzionePiano;
import pilot.servizi.entity.NoleggioManutenzione;
import pilot.servizi.entity.NoleggioManutenzioneConfigurazione;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.ClienteManutenzionePeriodoRepository;
import pilot.servizi.repository.ClienteManutenzionePianoRepository;
import pilot.servizi.repository.NoleggioManutenzioneConfigurazioneRepository;
import pilot.servizi.repository.NoleggioManutenzioneRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;
import pilot.servizi.repository.StoriaProdottiRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoleggioManutenzioneService {

    private final StoriaNoleggiRepository noleggiRepo;
    private final ClientiRepository clientiRepo;
    private final NoleggioManutenzioneConfigurazioneRepository configurazioniRepo;
    private final ClienteManutenzionePianoRepository pianiClienteRepo;
    private final ClienteManutenzionePeriodoRepository periodiClienteRepo;
    private final NoleggioManutenzioneRepository manutenzioniRepo;
    private final StoriaProdottiRepository prodottiRepo;

    @Transactional
    public NoleggioManutenzioneResponse configura(
            Integer docId,
            NoleggioManutenzioneConfigurazioneRequest request
    ) {
        validateConfigurazione(docId, request);
        StoriaNoleggi noleggio = getNoleggio(docId, "configura");
        requireCliente(noleggio, "configura");

        NoleggioManutenzioneConfigurazione config = configurazioniRepo.findById(docId)
                .orElseGet(() -> NoleggioManutenzioneConfigurazione.builder().docId(docId).build());
        config.setSogliaQuantita(request.sogliaQuantita() == null ? 1d : request.sogliaQuantita());
        config.setOrdinariePerStraordinaria(
                request.ordinariePerStraordinaria() == null ? 1 : request.ordinariePerStraordinaria()
        );
        config.setCategoriaIds(new HashSet<>(request.categoriaIds()));
        configurazioniRepo.save(config);
        return getStato(docId);
    }

    public NoleggioManutenzioneResponse getStato(Integer docId) {
        StoriaNoleggi noleggio = getNoleggio(docId, "getStato");
        Clienti cliente = requireCliente(noleggio, "getStato");
        NoleggioManutenzioneConfigurazione config = configurazioniRepo.findById(docId).orElse(null);
        NoleggioManutenzione ultimaManutenzioneOrdinaria = manutenzioniRepo
                .findFirstByDocIdAndTipoOrderByIdDesc(docId, NoleggioManutenzione.Tipo.ORDINARIA)
                .orElse(null);

        Double quantitaAcquistata = config == null ? 0d : quantitaAcquistata(cliente, config);
        int ordinarieDopoStraordinaria = contaOrdinarieDopoUltimaStraordinaria(docId);
        ClienteManutenzionePiano piano = pianiClienteRepo.findById(cliente.getId()).orElse(null);
        List<ClienteManutenzionePeriodo> periodi = piano == null
                ? List.of()
                : periodiClienteRepo.findByClienteIdOrderByDataInizioAsc(cliente.getId());
        LocalDate dataRiferimento = ultimaManutenzioneOrdinaria == null
                ? noleggio.getDataDoc()
                : ultimaManutenzioneOrdinaria.getEseguitaIl().toLocalDate();
        ClienteManutenzionePeriodo periodoApplicabile = periodoPerRiferimento(dataRiferimento, periodi);
        LocalDate prossimaManutenzione = calcolaProssimaManutenzione(dataRiferimento, periodoApplicabile);
        Long giorniAllaManutenzione = prossimaManutenzione == null
                ? null
                : ChronoUnit.DAYS.between(LocalDate.now(), prossimaManutenzione);
        boolean dovuta = giorniAllaManutenzione != null && giorniAllaManutenzione <= 0;
        String tipoDovuto = dovuta ? NoleggioManutenzione.Tipo.ORDINARIA.name() : null;
        String stato = statoManutenzione(piano, periodoApplicabile, giorniAllaManutenzione);

        return new NoleggioManutenzioneResponse(
                docId,
                noleggio.getClienteId(),
                nomeCliente(cliente),
                noleggio.getArticoloId(),
                noleggio.getMatricola(),
                noleggio.getQuantita(),
                piano != null,
                config == null ? null : config.getSogliaQuantita(),
                config == null ? null : config.getOrdinariePerStraordinaria(),
                config == null ? List.of() : config.getCategoriaIds().stream().sorted().toList(),
                quantitaAcquistata,
                ordinarieDopoStraordinaria,
                dovuta,
                tipoDovuto,
                ultimaManutenzioneOrdinaria == null ? null : ultimaManutenzioneOrdinaria.getEseguitaIl(),
                piano != null,
                prossimaManutenzione,
                giorniAllaManutenzione == null ? null : Math.toIntExact(giorniAllaManutenzione),
                stato,
                periodoApplicabile == null ? null : periodoApplicabile.getIntervalloGiorni()
        );
    }

    @Transactional
    public ClienteManutenzionePianificazioneResponse configuraPianificazioneCliente(
            Long clienteId,
            NoleggioManutenzionePianificazioneRequest request
    ) {
        validatePianificazione(clienteId, request);
        Clienti cliente = getCliente(clienteId, "configuraPianificazioneCliente");

        List<NoleggioManutenzionePianificazioneRequest.PeriodoRequest> periodi = request.periodi().stream()
                .sorted(Comparator.comparing(NoleggioManutenzionePianificazioneRequest.PeriodoRequest::dataInizio))
                .toList();
        for (int i = 1; i < periodi.size(); i++) {
            if (!periodi.get(i).dataInizio().isAfter(periodi.get(i - 1).dataFine())) {
                log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION,
                        "configuraPianificazioneCliente", ErrorConstants.MANUTENZIONE_PERIODI_SOVRAPPOSTI);
                throw new IllegalArgumentException(ErrorConstants.MANUTENZIONE_PERIODI_SOVRAPPOSTI);
            }
        }

        pianiClienteRepo.save(ClienteManutenzionePiano.builder()
                .clienteId(clienteId)
                .giorniAvviso(request.giorniAvviso())
                .build());
        periodiClienteRepo.deleteByClienteId(clienteId);
        periodiClienteRepo.saveAll(periodi.stream()
                .map(periodo -> ClienteManutenzionePeriodo.builder()
                        .clienteId(clienteId)
                        .dataInizio(periodo.dataInizio())
                        .dataFine(periodo.dataFine())
                        .intervalloGiorni(periodo.intervalloGiorni())
                        .build())
                .toList());
        return toPianificazioneResponse(
                cliente,
                pianiClienteRepo.findById(clienteId).orElseThrow(),
                periodiClienteRepo.findByClienteIdOrderByDataInizioAsc(clienteId)
        );
    }

    public ClienteManutenzionePianificazioneResponse getPianificazioneCliente(Long clienteId) {
        Clienti cliente = getCliente(clienteId, "getPianificazioneCliente");
        ClienteManutenzionePiano piano = pianiClienteRepo.findById(clienteId)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND,
                            "getPianificazioneCliente", ErrorConstants.MANUTENZIONE_PIANO_CLIENTE_NON_TROVATO + clienteId);
                    return new ResourceNotFoundException(ErrorConstants.MANUTENZIONE_PIANO_CLIENTE_NON_TROVATO + clienteId);
                });
        return toPianificazioneResponse(
                cliente,
                piano,
                periodiClienteRepo.findByClienteIdOrderByDataInizioAsc(clienteId)
        );
    }

    private ClienteManutenzionePianificazioneResponse toPianificazioneResponse(
            Clienti cliente,
            ClienteManutenzionePiano piano,
            List<ClienteManutenzionePeriodo> periodi
    ) {
        return new ClienteManutenzionePianificazioneResponse(
                cliente.getId(),
                piano.getGiorniAvviso(),
                periodi.stream()
                        .map(periodo -> new NoleggioManutenzionePeriodoResponse(
                                periodo.getDataInizio(), periodo.getDataFine(), periodo.getIntervalloGiorni()
                        ))
                        .toList()
        );
    }

    private Clienti getCliente(Long clienteId, String operation) {
        return clientiRepo.findById(clienteId)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND,
                            operation, ErrorConstants.CLIENTE_NON_TROVATO + clienteId);
                    return new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + clienteId);
                });
    }

    public List<NoleggioManutenzioneEventoResponse> getEventi(Integer docId) {
        getNoleggio(docId, "getEventi");
        return getEventiEntity(docId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public NoleggioManutenzioneEventoResponse registraManutenzione(
            Integer docId,
            NoleggioManutenzioneRequest request
    ) {
        if (request == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "registraManutenzione", ErrorConstants.MANUTENZIONE_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.MANUTENZIONE_DTO_NULL);
        }
        NoleggioManutenzioneResponse stato = getStato(docId);
        if (!stato.manutenzioneDovuta()) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "registraManutenzione", ErrorConstants.MANUTENZIONE_NON_DOVUTA + docId);
            throw new IllegalArgumentException(ErrorConstants.MANUTENZIONE_NON_DOVUTA + docId);
        }

        NoleggioManutenzione evento = NoleggioManutenzione.builder()
                .docId(docId)
                .tipo(NoleggioManutenzione.Tipo.valueOf(stato.tipoManutenzioneDovuta()))
                .quantitaAcquistataTotale(stato.quantitaAcquistataTotale())
                .eseguitaIl(LocalDateTime.now())
                .note(request.note())
                .build();
        return toResponse(manutenzioniRepo.save(evento));
    }

    @Transactional
    public NoleggioManutenzioneEventoResponse registraStraordinaria(
            Integer docId,
            NoleggioManutenzioneRequest request
    ) {
        if (request == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION,
                    "registraStraordinaria", ErrorConstants.MANUTENZIONE_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.MANUTENZIONE_DTO_NULL);
        }
        StoriaNoleggi noleggio = getNoleggio(docId, "registraStraordinaria");
        Clienti cliente = requireCliente(noleggio, "registraStraordinaria");
        NoleggioManutenzioneConfigurazione config = configurazioniRepo.findById(docId).orElse(null);
        NoleggioManutenzione evento = NoleggioManutenzione.builder()
                .docId(docId)
                .tipo(NoleggioManutenzione.Tipo.STRAORDINARIA)
                .quantitaAcquistataTotale(config == null ? 0d : quantitaAcquistata(cliente, config))
                .eseguitaIl(LocalDateTime.now())
                .note(request.note())
                .build();
        return toResponse(manutenzioniRepo.save(evento));
    }

    private ClienteManutenzionePeriodo periodoPerRiferimento(
            LocalDate dataRiferimento,
            List<ClienteManutenzionePeriodo> periodi
    ) {
        if (dataRiferimento == null) {
            return null;
        }
        return periodi.stream()
                .filter(periodo -> !dataRiferimento.isBefore(periodo.getDataInizio())
                        && !dataRiferimento.isAfter(periodo.getDataFine()))
                .findFirst()
                .or(() -> periodi.stream()
                        .filter(periodo -> periodo.getDataInizio().isAfter(dataRiferimento))
                        .findFirst())
                .orElse(null);
    }

    private LocalDate calcolaProssimaManutenzione(
            LocalDate dataRiferimento,
            ClienteManutenzionePeriodo periodo
    ) {
        if (dataRiferimento == null || periodo == null) {
            return null;
        }
        LocalDate dataBase = dataRiferimento.isBefore(periodo.getDataInizio())
                ? periodo.getDataInizio()
                : dataRiferimento;
        return dataBase.plusDays(periodo.getIntervalloGiorni());
    }

    private String statoManutenzione(
            ClienteManutenzionePiano piano,
            ClienteManutenzionePeriodo periodoApplicabile,
            Long giorniAllaManutenzione
    ) {
        if (piano == null || giorniAllaManutenzione == null || periodoApplicabile == null) {
            return "NON_CONFIGURATA";
        }
        if (giorniAllaManutenzione <= 0) {
            return "IN_RITARDO";
        }
        if (giorniAllaManutenzione <= piano.getGiorniAvviso()) {
            return "IN_SCADENZA";
        }
        return "PROGRAMMATA";
    }

    private void validateConfigurazione(Integer docId, NoleggioManutenzioneConfigurazioneRequest request) {
        if (docId == null || request == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "configura", ErrorConstants.MANUTENZIONE_DTO_NULL);
            throw new IllegalArgumentException(ErrorConstants.MANUTENZIONE_DTO_NULL);
        }
        if (request.sogliaQuantita() != null && request.sogliaQuantita() <= 0
                || request.ordinariePerStraordinaria() != null && request.ordinariePerStraordinaria() <= 0
                || request.categoriaIds() == null || request.categoriaIds().isEmpty()
                || request.categoriaIds().stream().anyMatch(id -> id == null || id <= 0)) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "configura", ErrorConstants.MANUTENZIONE_CONFIGURAZIONE_INVALIDA);
            throw new IllegalArgumentException(ErrorConstants.MANUTENZIONE_CONFIGURAZIONE_INVALIDA);
        }
    }

    private void validatePianificazione(Long clienteId, NoleggioManutenzionePianificazioneRequest request) {
        if (clienteId == null || request == null
                || request.giorniAvviso() == null || request.giorniAvviso() < 0
                || request.periodi() == null || request.periodi().isEmpty()
                || request.periodi().stream().anyMatch(periodo ->
                        periodo == null
                                || periodo.dataInizio() == null
                                || periodo.dataFine() == null
                                || periodo.dataInizio().isAfter(periodo.dataFine())
                                || periodo.intervalloGiorni() == null
                                || periodo.intervalloGiorni() <= 0)) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION,
                    "configuraPianificazioneCliente", ErrorConstants.MANUTENZIONE_PIANIFICAZIONE_INVALIDA);
            throw new IllegalArgumentException(ErrorConstants.MANUTENZIONE_PIANIFICAZIONE_INVALIDA);
        }
    }

    private StoriaNoleggi getNoleggio(Integer docId, String operation) {
        return noleggiRepo.findById(docId)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, operation, ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
                    return new ResourceNotFoundException(ErrorConstants.NOLEGGIO_NON_TROVATO + docId);
                });
    }

    private Clienti requireCliente(StoriaNoleggi noleggio, String operation) {
        if (noleggio.getClienteId() == null) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, operation, ErrorConstants.MANUTENZIONE_NOLEGGIO_SENZA_CLIENTE + noleggio.getDocId());
            throw new IllegalArgumentException(ErrorConstants.MANUTENZIONE_NOLEGGIO_SENZA_CLIENTE + noleggio.getDocId());
        }
        return clientiRepo.findById(noleggio.getClienteId())
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND, operation, ErrorConstants.CLIENTE_NON_TROVATO + noleggio.getClienteId());
                    return new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + noleggio.getClienteId());
                });
    }

    private Double quantitaAcquistata(Clienti cliente, NoleggioManutenzioneConfigurazione config) {
        if (config.getCategoriaIds().isEmpty()) {
            return 0d;
        }
        return prodottiRepo.sumQuantitaByClienteIdAndCategoriaIdIn(
                cliente.getId(),
                config.getCategoriaIds().stream().sorted().toList()
        );
    }

    private List<NoleggioManutenzione> getEventiEntity(Integer docId) {
        return manutenzioniRepo.findByDocIdOrderByIdAsc(docId);
    }

    private int contaOrdinarieDopoUltimaStraordinaria(Integer docId) {
        long ultimaStraordinariaId = manutenzioniRepo
                .findFirstByDocIdAndTipoOrderByIdDesc(docId, NoleggioManutenzione.Tipo.STRAORDINARIA)
                .map(NoleggioManutenzione::getId)
                .orElse(0L);
        return Math.toIntExact(manutenzioniRepo.countByDocIdAndTipoAndIdGreaterThan(
                docId,
                NoleggioManutenzione.Tipo.ORDINARIA,
                ultimaStraordinariaId
        ));
    }

    private String nomeCliente(Clienti cliente) {
        return cliente.getRagioneSociale() != null && !cliente.getRagioneSociale().isBlank()
                ? cliente.getRagioneSociale()
                : String.join(" ", List.of(
                        cliente.getNome() == null ? "" : cliente.getNome(),
                        cliente.getCognome() == null ? "" : cliente.getCognome()
                )).trim();
    }

    private NoleggioManutenzioneEventoResponse toResponse(NoleggioManutenzione evento) {
        return new NoleggioManutenzioneEventoResponse(
                evento.getId(),
                evento.getDocId(),
                evento.getTipo().name(),
                evento.getQuantitaAcquistataTotale(),
                evento.getEseguitaIl(),
                evento.getNote()
        );
    }
}
