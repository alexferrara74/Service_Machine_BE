package pilot.servizi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pilot.servizi.entity.StoriaProdotti;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class StoriaProdottiRepositoryTest {

    @Autowired
    private StoriaProdottiRepository repository;

    @Test
    void sumsOnlyQuantitiesForCustomerAndSelectedCategories() {
        repository.save(StoriaProdotti.builder()
                .clienteId(7L)
                .categoriaId(4)
                .quantita(2.5d)
                .build());
        repository.save(StoriaProdotti.builder()
                .clienteId(7L)
                .categoriaId(7)
                .quantita(3d)
                .build());
        repository.save(StoriaProdotti.builder()
                .clienteId(7L)
                .categoriaId(8)
                .quantita(100d)
                .build());
        repository.save(StoriaProdotti.builder()
                .clienteId(8L)
                .categoriaId(4)
                .quantita(50d)
                .build());

        assertEquals(5.5d, repository.sumQuantitaByClienteIdAndCategoriaIdIn(7L, List.of(4, 7)));
    }

    @Test
    void aggregatesCustomerSalesByProductCategoryAndMonthWithinDateRange() {
        repository.save(StoriaProdotti.builder()
                .clienteId(7L).articoloId(10L).codiceArticolo("CAFFE-1")
                .descrizione("Caffè").categoriaId(4).categoriaDescrizione("Chicchi")
                .quantita(2d).dataDoc(LocalDate.of(2026, 6, 10)).build());
        repository.save(StoriaProdotti.builder()
                .clienteId(7L).articoloId(10L).codiceArticolo("CAFFE-1")
                .descrizione("Caffè").categoriaId(4).categoriaDescrizione("Chicchi")
                .quantita(3d).dataDoc(LocalDate.of(2026, 6, 20)).build());
        repository.save(StoriaProdotti.builder()
                .clienteId(7L).articoloId(11L).codiceArticolo("SOLUBILE-1")
                .descrizione("Solubile").categoriaId(7).categoriaDescrizione("Solubili")
                .quantita(7d).dataDoc(LocalDate.of(2026, 7, 5)).build());
        repository.save(StoriaProdotti.builder()
                .clienteId(8L).articoloId(10L).codiceArticolo("CAFFE-1")
                .descrizione("Caffè").categoriaId(4).categoriaDescrizione("Chicchi")
                .quantita(100d).dataDoc(LocalDate.of(2026, 6, 10)).build());

        var categorie = repository.statistichePerCategoria(
                7L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)
        );
        var prodotti = repository.statistichePerProdotto(
                7L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)
        );
        var mensili = repository.statisticheMensili(
                7L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30)
        );

        assertEquals(1, categorie.size());
        assertEquals(5d, categorie.get(0).getQuantitaTotale());
        assertEquals(1, prodotti.size());
        assertEquals(2L, prodotti.get(0).getRigheVendita());
        assertEquals(1, mensili.size());
        assertEquals(6, mensili.get(0).getMese());
        assertEquals(5d, mensili.get(0).getQuantitaTotale());
    }
}
