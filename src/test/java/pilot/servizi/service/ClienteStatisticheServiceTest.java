package pilot.servizi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pilot.servizi.entity.Clienti;
import pilot.servizi.entity.NoleggioManutenzioneConfigurazione;
import pilot.servizi.entity.StoriaNoleggi;
import pilot.servizi.repository.ClientiRepository;
import pilot.servizi.repository.NoleggioManutenzioneConfigurazioneRepository;
import pilot.servizi.repository.StoriaNoleggiRepository;
import pilot.servizi.repository.StoriaProdottiRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteStatisticheServiceTest {

    @Mock
    private ClientiRepository clientiRepo;
    @Mock
    private StoriaNoleggiRepository noleggiRepo;
    @Mock
    private StoriaProdottiRepository prodottiRepo;
    @Mock
    private NoleggioManutenzioneConfigurazioneRepository configurazioniRepo;

    @Test
    void reportsCustomerPurchaseEstimateForEachConfiguredRentalWithoutAllocatingItToMachines() {
        var noleggioA = StoriaNoleggi.builder()
                .docId(101).clienteId(7L).articoloId(55L).matricola("MAC-A").quantita(1d).build();
        var noleggioB = StoriaNoleggi.builder()
                .docId(102).clienteId(7L).articoloId(55L).matricola("MAC-B").quantita(1d).build();
        var categoria = org.mockito.Mockito.mock(StoriaProdottiRepository.CategoriaVenditeProjection.class);
        when(categoria.getCategoriaId()).thenReturn(4);
        when(categoria.getCategoriaDescrizione()).thenReturn("Caffè");
        when(categoria.getQuantitaTotale()).thenReturn(25d);
        when(categoria.getRigheVendita()).thenReturn(3L);

        var prodotto = org.mockito.Mockito.mock(StoriaProdottiRepository.ProdottoVenditeProjection.class);
        when(prodotto.getArticoloId()).thenReturn(900L);
        when(prodotto.getCodiceArticolo()).thenReturn("CAFFE-1");
        when(prodotto.getDescrizione()).thenReturn("Caffè in grani");
        when(prodotto.getCategoriaId()).thenReturn(4);
        when(prodotto.getCategoriaDescrizione()).thenReturn("Caffè");
        when(prodotto.getQuantitaTotale()).thenReturn(25d);
        when(prodotto.getRigheVendita()).thenReturn(3L);

        when(clientiRepo.findById(7L)).thenReturn(Optional.of(Clienti.builder()
                .id(7L).ragioneSociale("Cliente Test").build()));
        when(noleggiRepo.findByClienteId(7L)).thenReturn(List.of(noleggioA, noleggioB));
        when(configurazioniRepo.findAllById(List.of(101, 102))).thenReturn(List.of(
                NoleggioManutenzioneConfigurazione.builder().docId(101).categoriaIds(Set.of(4)).build(),
                NoleggioManutenzioneConfigurazione.builder().docId(102).categoriaIds(Set.of(4)).build()
        ));
        when(prodottiRepo.statistichePerCategoria(7L, null, null)).thenReturn(List.of(categoria));
        when(prodottiRepo.statistichePerProdotto(7L, null, null)).thenReturn(List.of(prodotto));
        when(prodottiRepo.statisticheMensili(7L, null, null)).thenReturn(List.of());

        var service = new ClienteStatisticheService(
                clientiRepo, noleggiRepo, prodottiRepo, configurazioniRepo
        );
        var result = service.getStatistiche(7L, null, null);

        assertEquals(2, result.noleggiEStimePerAttrezzatura().size());
        assertEquals(25d, result.noleggiEStimePerAttrezzatura().get(0)
                .prodottiAcquistatiStimati().get(0).quantitaTotale());
        assertEquals(
                "TOTALE_CLIENTE_NON_ATTRIBUITO_A_SINGOLA_MACCHINA",
                result.noleggiEStimePerAttrezzatura().get(1).ambitoStima()
        );
    }
}
