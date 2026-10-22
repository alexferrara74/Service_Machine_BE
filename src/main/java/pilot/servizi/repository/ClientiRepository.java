package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.Clienti;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientiRepository extends JpaRepository<Clienti, Long> {

    Optional<Clienti> findByCodice(String codice);
    boolean existsByCodice(String codice);

    List<Clienti> findByRagioneSocialeContainingIgnoreCase(String ragioneSociale);
    List<Clienti> findByEmailIgnoreCase(String email);

    // Per la sincronizzazione delta
    List<Clienti> findByAggiornatoIlAfter(LocalDateTime since);

    @Query("SELECT c FROM Clienti c WHERE c.partitaIva = :piva OR c.codiceFiscale = :cf")
    Optional<Clienti> findByPartitaIvaOrCodiceFiscale(@Param("piva") String piva,
                                                           @Param("cf") String cf);
}