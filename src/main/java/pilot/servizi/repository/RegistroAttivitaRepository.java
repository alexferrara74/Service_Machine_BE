package pilot.servizi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.RegistroAttivita;

import java.time.LocalDateTime;

@Repository
public interface RegistroAttivitaRepository extends JpaRepository<RegistroAttivita, Long> {

    /** Filtri tutti facoltativi; {@code a} è escluso. L'ordinamento arriva dal Pageable. */
    @Query("""
            select r from RegistroAttivita r
            where (:username is null or lower(r.username) like lower(concat('%', :username, '%')))
              and (:azione is null or r.azione = :azione)
              and (:esito is null or r.esito = :esito)
              and (:da is null or r.quando >= :da)
              and (:a is null or r.quando < :a)
            """)
    Page<RegistroAttivita> cerca(
            @Param("username") String username,
            @Param("azione") String azione,
            @Param("esito") String esito,
            @Param("da") LocalDateTime da,
            @Param("a") LocalDateTime a,
            Pageable pageable
    );
}
