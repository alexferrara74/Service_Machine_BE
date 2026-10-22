package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.Interventi;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterventiRepository extends JpaRepository<Interventi, Long> {

    List<Interventi> findByClienteId(Long clienteId);
    List<Interventi> findByStato(String stato);
    List<Interventi> findByStatoNot(String stato);
    List<Interventi> findByMatricolaAttrezzatura(String matricolaAttrezzatura);

    List<Interventi> findByDataAperturaBetween(LocalDateTime from, LocalDateTime to);
    List<Interventi> findByDataChiusuraIsNull();   // interventi aperti
    List<Interventi> findByDataChiusuraIsNotNull(); // interventi chiusi

    List<Interventi> findByClienteIdAndStato(Long clienteId, String stato);

    List<Interventi> findByDataAperturaAfter(LocalDateTime since);

    // Ricerca testuale su titolo + descrizione
    @Query("SELECT i FROM Interventi i " +
            "WHERE LOWER(i.titolo) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "   OR LOWER(i.descrizioneProblema) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Interventi> search(@Param("q") String query);
}