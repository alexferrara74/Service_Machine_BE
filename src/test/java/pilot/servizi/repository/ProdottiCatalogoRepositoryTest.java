package pilot.servizi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pilot.servizi.entity.ProdottiCatalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ProdottiCatalogoRepositoryTest {

    @Autowired
    private ProdottiCatalogoRepository repository;

    @Test
    void filtersCatalogByEquipmentAndCategory() {
        repository.save(product(1L, "PROD-1", 5, false));
        repository.save(product(2L, "ATT-1", 5, true));
        repository.save(product(3L, "ATT-2", 7, true));

        assertEquals(2, repository.findByCategoriaIdOrderByCodiceAsc(5).size());
        assertEquals(2, repository.findByIsAttrezzaturaTrueOrderByCodiceAsc().size());
        assertEquals(1, repository.findByCategoriaIdAndIsAttrezzaturaOrderByCodiceAsc(5, true).size());
    }

    @Test
    void searchSeparatesProductsEquipmentAndAccessories() {
        repository.save(product(1L, "PROD-1", 5, false));
        ProdottiCatalogo accessory = product(2L, "ACC-1", 5, false);
        accessory.setIsAccessorio(true);
        repository.save(accessory);
        repository.save(product(3L, "ATT-1", 5, true));

        assertEquals(3, repository.search(null, null, null).size());
        assertEquals(1, repository.search(null, false, false).size());
        assertEquals("ACC-1", repository.search(null, null, true).get(0).getCodice());
        assertEquals(1, repository.search(5, true, null).size());
    }

    private ProdottiCatalogo product(Long id, String code, Integer category, boolean equipment) {
        ProdottiCatalogo product = new ProdottiCatalogo();
        product.setArticoloId(id);
        product.setCodice(code);
        product.setDescrizione(code);
        product.setCategoriaId(category);
        product.setIsAttrezzatura(equipment);
        return product;
    }
}
