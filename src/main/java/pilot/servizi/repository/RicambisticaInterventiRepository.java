package pilot.servizi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.Ricambi;

import java.util.List;

@Repository
public interface RicambisticaInterventiRepository extends JpaRepository<Ricambi, Long> {

    List<Ricambi> findByInterventoId(Long interventoId);
    List<Ricambi> findByArticoloId(Long articoloId);
    List<Ricambi> findByCodiceRicambio(String codiceRicambio);

    // Cancellazione in blocco dei ricambi di un intervento
    void deleteByInterventoId(Long interventoId);

    // Totale pezzi usati per un articolo (utile per statistiche)
    @Query("SELECT COALESCE(SUM(r.quantitaUtilizzata), 0) FROM Ricambi r WHERE r.articoloId = :articoloId")
    Integer sumQuantitaByArticoloId(@Param("articoloId") Long articoloId);

    // Totale costo ricambi di un intervento
    @Query("SELECT COALESCE(SUM(r.quantitaUtilizzata * r.prezzoUnitario), 0) " +
            "FROM Ricambi r WHERE r.interventoId = :interventoId")
    Double sumCostoByInterventoId(@Param("interventoId") Long interventoId);
}