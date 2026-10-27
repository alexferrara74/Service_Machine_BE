package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.StoriaProdotti;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StoriaProdottiRepository extends JpaRepository<StoriaProdotti, Long> {

    interface CategoriaVenditeProjection {
        Integer getCategoriaId();
        String getCategoriaDescrizione();
        Double getQuantitaTotale();
        Long getRigheVendita();
    }

    interface ProdottoVenditeProjection {
        Long getArticoloId();
        String getCodiceArticolo();
        String getDescrizione();
        Integer getCategoriaId();
        String getCategoriaDescrizione();
        Double getQuantitaTotale();
        Long getRigheVendita();
    }

    interface VenditeMensiliProjection {
        Integer getAnno();
        Integer getMese();
        Double getQuantitaTotale();
        Long getRigheVendita();
    }

    List<StoriaProdotti> findByClienteId(Long clienteId);
    List<StoriaProdotti> findByArticoloId(Long articoloId);
    List<StoriaProdotti> findByCodiceArticolo(String codiceArticolo);
    List<StoriaProdotti> findByCategoriaId(Integer categoriaId);
    List<StoriaProdotti> findByAggiornatoIlAfter(LocalDateTime since);

    @Query("""
            SELECT COALESCE(SUM(p.quantita), 0)
            FROM StoriaProdotti p
            WHERE p.clienteId = :clienteId
              AND p.categoriaId IN :categoriaIds
            """)
    Double sumQuantitaByClienteIdAndCategoriaIdIn(
            @Param("clienteId") Long clienteId,
            @Param("categoriaIds") List<Integer> categoriaIds
    );

    @Query("""
            SELECT p.categoriaId AS categoriaId,
                   p.categoriaDescrizione AS categoriaDescrizione,
                   SUM(p.quantita) AS quantitaTotale,
                   COUNT(p.id) AS righeVendita
            FROM StoriaProdotti p
            WHERE p.clienteId = :clienteId
              AND (:dal IS NULL OR p.dataDoc >= :dal)
              AND (:al IS NULL OR p.dataDoc <= :al)
            GROUP BY p.categoriaId, p.categoriaDescrizione
            ORDER BY SUM(p.quantita) DESC
            """)
    List<CategoriaVenditeProjection> statistichePerCategoria(
            @Param("clienteId") Long clienteId,
            @Param("dal") LocalDate dal,
            @Param("al") LocalDate al
    );

    @Query("""
            SELECT p.articoloId AS articoloId,
                   p.codiceArticolo AS codiceArticolo,
                   p.descrizione AS descrizione,
                   p.categoriaId AS categoriaId,
                   p.categoriaDescrizione AS categoriaDescrizione,
                   SUM(p.quantita) AS quantitaTotale,
                   COUNT(p.id) AS righeVendita
            FROM StoriaProdotti p
            WHERE p.clienteId = :clienteId
              AND (:dal IS NULL OR p.dataDoc >= :dal)
              AND (:al IS NULL OR p.dataDoc <= :al)
            GROUP BY p.articoloId, p.codiceArticolo, p.descrizione,
                     p.categoriaId, p.categoriaDescrizione
            ORDER BY SUM(p.quantita) DESC
            """)
    List<ProdottoVenditeProjection> statistichePerProdotto(
            @Param("clienteId") Long clienteId,
            @Param("dal") LocalDate dal,
            @Param("al") LocalDate al
    );

    @Query("""
            SELECT YEAR(p.dataDoc) AS anno,
                   MONTH(p.dataDoc) AS mese,
                   SUM(p.quantita) AS quantitaTotale,
                   COUNT(p.id) AS righeVendita
            FROM StoriaProdotti p
            WHERE p.clienteId = :clienteId
              AND p.dataDoc IS NOT NULL
              AND (:dal IS NULL OR p.dataDoc >= :dal)
              AND (:al IS NULL OR p.dataDoc <= :al)
            GROUP BY YEAR(p.dataDoc), MONTH(p.dataDoc)
            ORDER BY YEAR(p.dataDoc), MONTH(p.dataDoc)
            """)
    List<VenditeMensiliProjection> statisticheMensili(
            @Param("clienteId") Long clienteId,
            @Param("dal") LocalDate dal,
            @Param("al") LocalDate al
    );
}
