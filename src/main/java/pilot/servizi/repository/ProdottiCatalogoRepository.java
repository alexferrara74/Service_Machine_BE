package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.ProdottiCatalogo;

import java.util.List;

@Repository
public interface ProdottiCatalogoRepository extends JpaRepository<ProdottiCatalogo, Long> {

    List<ProdottiCatalogo> findAllByOrderByCodiceAsc();
    List<ProdottiCatalogo> findByIsAttrezzaturaTrueOrderByCodiceAsc();
    List<ProdottiCatalogo> findByIsAttrezzaturaFalseOrderByCodiceAsc();
    List<ProdottiCatalogo> findByCategoriaIdOrderByCodiceAsc(Integer categoriaId);
    List<ProdottiCatalogo> findByCategoriaIdAndIsAttrezzaturaOrderByCodiceAsc(
            Integer categoriaId,
            Boolean isAttrezzatura
    );
}
