package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.MagazzinoPosizioniArticoli;

import java.util.List;
import java.util.Optional;

@Repository
public interface MagazzinoPosizioniArticoliRepository extends JpaRepository<MagazzinoPosizioniArticoli, Long> {

    List<MagazzinoPosizioniArticoli> findByUbicazioneId(Long ubicazioneId);
    List<MagazzinoPosizioniArticoli> findByArticoloId(Long articoloId);
    Optional<MagazzinoPosizioniArticoli> findByUbicazioneIdAndArticoloId(Long ubicazioneId, Long articoloId);
    boolean existsByUbicazioneIdAndArticoloId(Long ubicazioneId, Long articoloId);

    // Somma totale della giacenza di un articolo su tutte le ubicazioni
    @Query("SELECT COALESCE(SUM(p.quantitaPresente), 0) FROM MagazzinoPosizioniArticoli p WHERE p.articoloId = :articoloId")
    Integer sumQuantitaByArticoloId(@Param("articoloId") Long articoloId);

    // Aggiornamento "delta" della quantità (usato dai movimenti)
    @Modifying
    @Query("UPDATE MagazzinoPosizioniArticoli p SET p.quantitaPresente = p.quantitaPresente + :delta " +
            "WHERE p.ubicazioneId = :ubicazioneId AND p.articoloId = :articoloId")
    int updateQuantita(@Param("ubicazioneId") Long ubicazioneId,
                       @Param("articoloId") Long articoloId,
                       @Param("delta") Integer delta);
}