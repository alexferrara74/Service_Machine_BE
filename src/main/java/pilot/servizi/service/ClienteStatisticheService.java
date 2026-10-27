package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.dto.response.CategoriaVenditeStatisticheResponse;
import pilot.servizi.dto.response.ClienteStatisticheResponse;
import pilot.servizi.dto.response.NoleggioVenditeStimateResponse;
import pilot.servizi.dto.response.ProdottoVenditeStatisticheResponse;
import pilot.servizi.dto.response.VenditeMensiliStatisticheResponse;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.NoleggioManutenzioneConfigurazione;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.log.ErrorConstants;
import pilot.servizi.log.ResourceNotFoundException;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.NoleggioManutenzioneConfigurazioneRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;
import pilot.servizi.repository.StoriaProdottiRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteStatisticheService {

    private static final String AMBITO_STIMA = "TOTALE_CLIENTE_NON_ATTRIBUITO_A_SINGOLA_MACCHINA";

    private final ClientiRepository clientiRepo;
    private final StoriaNoleggiRepository noleggiRepo;
    private final StoriaProdottiRepository prodottiRepo;
    private final NoleggioManutenzioneConfigurazioneRepository configurazioniRepo;

    public ClienteStatisticheResponse getStatistiche(Long clienteId, LocalDate dal, LocalDate al) {
        if (clienteId == null || dal != null && al != null && dal.isAfter(al)) {
            log.warn(ErrorConstants.LOG_VALIDATION, ErrorConstants.ERR_VALIDATION, "getStatisticheCliente",
                    ErrorConstants.STATISTICHE_INTERVALLO_INVALIDO);
            throw new IllegalArgumentException(ErrorConstants.STATISTICHE_INTERVALLO_INVALIDO);
        }

        Clienti cliente = clientiRepo.findById(clienteId)
                .orElseThrow(() -> {
                    log.warn(ErrorConstants.LOG_NOT_FOUND, ErrorConstants.ERR_NOT_FOUND,
                            "getStatisticheCliente", ErrorConstants.CLIENTE_NON_TROVATO + clienteId);
                    return new ResourceNotFoundException(ErrorConstants.CLIENTE_NON_TROVATO + clienteId);
                });

        List<StoriaNoleggi> noleggi = noleggiRepo.findByClienteId(clienteId);
        Map<Integer, NoleggioManutenzioneConfigurazione> configurazioni = configurazioniRepo
                .findAllById(noleggi.stream().map(StoriaNoleggi::getDocId).toList())
                .stream()
                .collect(Collectors.toMap(NoleggioManutenzioneConfigurazione::getDocId, Function.identity()));

        List<CategoriaVenditeStatisticheResponse> categorieRaw = prodottiRepo
                .statistichePerCategoria(clienteId, dal, al)
                .stream()
                .map(p -> new CategoriaVenditeStatisticheResponse(
                        p.getCategoriaId(), p.getCategoriaDescrizione(), p.getQuantitaTotale(), p.getRigheVendita()
                ))
                .toList();
        Map<Integer, CategoriaVenditeStatisticheResponse> categorieById = categorieRaw.stream()
                .filter(c -> c.categoriaId() != null)
                .collect(Collectors.toMap(
                        CategoriaVenditeStatisticheResponse::categoriaId,
                        Function.identity(),
                        this::mergeCategorie
                ));
        List<CategoriaVenditeStatisticheResponse> categorie = java.util.stream.Stream.concat(
                        categorieById.values().stream(),
                        categorieRaw.stream().filter(c -> c.categoriaId() == null)
                )
                .sorted(Comparator.comparing(
                        CategoriaVenditeStatisticheResponse::categoriaId,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();

        List<ProdottoVenditeStatisticheResponse> prodotti = prodottiRepo
                .statistichePerProdotto(clienteId, dal, al)
                .stream()
                .map(p -> new ProdottoVenditeStatisticheResponse(
                        p.getArticoloId(),
                        p.getCodiceArticolo(),
                        p.getDescrizione(),
                        p.getCategoriaId(),
                        p.getCategoriaDescrizione(),
                        p.getQuantitaTotale(),
                        p.getRigheVendita()
                ))
                .toList();
        List<VenditeMensiliStatisticheResponse> mensili = prodottiRepo.statisticheMensili(clienteId, dal, al)
                .stream()
                .map(p -> new VenditeMensiliStatisticheResponse(
                        p.getAnno(), p.getMese(), p.getQuantitaTotale(), p.getRigheVendita()
                ))
                .toList();

        List<NoleggioVenditeStimateResponse> stime = noleggi.stream()
                .map(noleggio -> stimaPerNoleggio(
                        noleggio,
                        configurazioni.get(noleggio.getDocId()),
                        categorieById,
                        prodotti
                ))
                .toList();

        double quantitaTotale = prodotti.stream()
                .mapToDouble(p -> p.quantitaTotale() == null ? 0d : p.quantitaTotale())
                .sum();
        long righeVendita = prodotti.stream()
                .mapToLong(p -> p.righeVendita() == null ? 0L : p.righeVendita())
                .sum();
        return new ClienteStatisticheResponse(
                clienteId,
                nomeCliente(cliente),
                dal,
                al,
                quantitaTotale,
                righeVendita,
                stime,
                categorie,
                prodotti,
                mensili
        );
    }

    private NoleggioVenditeStimateResponse stimaPerNoleggio(
            StoriaNoleggi noleggio,
            NoleggioManutenzioneConfigurazione configurazione,
            Map<Integer, CategoriaVenditeStatisticheResponse> categorieById,
            List<ProdottoVenditeStatisticheResponse> prodotti
    ) {
        Set<Integer> categoriaIds = configurazione == null
                ? Set.of()
                : configurazione.getCategoriaIds();
        List<CategoriaVenditeStatisticheResponse> categorieNoleggio =
                categoriaIds.isEmpty()
                        ? List.of()
                        : categoriaIds.stream()
                                .map(categorieById::get)
                                .filter(categoria -> categoria != null)
                                .sorted((first, second) -> first.categoriaId().compareTo(second.categoriaId()))
                                .toList();
        List<ProdottoVenditeStatisticheResponse> prodottiNoleggio = prodotti.stream()
                .filter(prodotto -> categoriaIds.contains(prodotto.categoriaId()))
                .toList();
        return new NoleggioVenditeStimateResponse(
                noleggio.getDocId(),
                noleggio.getArticoloId(),
                noleggio.getMatricola(),
                noleggio.getQuantita(),
                categorieNoleggio,
                prodottiNoleggio,
                AMBITO_STIMA
        );
    }

    private CategoriaVenditeStatisticheResponse mergeCategorie(
            CategoriaVenditeStatisticheResponse first,
            CategoriaVenditeStatisticheResponse second
    ) {
        return new CategoriaVenditeStatisticheResponse(
                first.categoriaId(),
                first.categoria() != null ? first.categoria() : second.categoria(),
                valore(first.quantitaTotale()) + valore(second.quantitaTotale()),
                righe(first.righeVendita()) + righe(second.righeVendita())
        );
    }

    private double valore(Double value) {
        return value == null ? 0d : value;
    }

    private long righe(Long value) {
        return value == null ? 0L : value;
    }

    private String nomeCliente(Clienti cliente) {
        if (cliente.getRagioneSociale() != null && !cliente.getRagioneSociale().isBlank()) {
            return cliente.getRagioneSociale();
        }
        return String.join(" ", List.of(
                cliente.getNome() == null ? "" : cliente.getNome(),
                cliente.getCognome() == null ? "" : cliente.getCognome()
        )).trim();
    }
}
