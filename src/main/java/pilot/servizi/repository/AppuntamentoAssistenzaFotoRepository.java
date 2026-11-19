package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.AppuntamentoAssistenzaFoto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppuntamentoAssistenzaFotoRepository extends JpaRepository<AppuntamentoAssistenzaFoto, Long> {

    /** Solo id e nome: l'elenco del calendario non deve caricare i contenuti delle foto. */
    interface Info {
        Long getId();
        Long getAppuntamentoId();
        String getNomeFile();
    }

    List<Info> findByAppuntamentoIdInOrderByIdAsc(Collection<Long> appuntamentoIds);

    Optional<AppuntamentoAssistenzaFoto> findByIdAndAppuntamentoId(Long id, Long appuntamentoId);

    long countByAppuntamentoId(Long appuntamentoId);

    @Modifying
    @Query("delete from AppuntamentoAssistenzaFoto f where f.appuntamentoId = :id")
    void deleteByAppuntamentoId(@Param("id") Long id);
}
