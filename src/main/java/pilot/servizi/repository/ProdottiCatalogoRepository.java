package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.ProdottiCatalogo;

import java.util.List;

@Repository
public interface ProdottiCatalogoRepository extends JpaRepository<ProdottiCatalogo, Long> {

    @Query("""
            select p from ProdottiCatalogo p
            where (:categoriaId is null or p.categoriaId = :categoriaId)
              and (:attrezzatura is null or p.isAttrezzatura = :attrezzatura)
              and (:accessorio is null or p.isAccessorio = :accessorio)
            order by p.codice asc
            """)
    List<ProdottiCatalogo> search(
            @Param("categoriaId") Integer categoriaId,
            @Param("attrezzatura") Boolean attrezzatura,
            @Param("accessorio") Boolean accessorio
    );

    List<ProdottiCatalogo> findAllByOrderByCodiceAsc();
    List<ProdottiCatalogo> findByIsAttrezzaturaTrueOrderByCodiceAsc();
    List<ProdottiCatalogo> findByIsAttrezzaturaFalseOrderByCodiceAsc();
    List<ProdottiCatalogo> findByCategoriaIdOrderByCodiceAsc(Integer categoriaId);
    List<ProdottiCatalogo> findByCategoriaIdAndIsAttrezzaturaOrderByCodiceAsc(
            Integer categoriaId,
            Boolean isAttrezzatura
    );
}
