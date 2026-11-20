package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.AppuntamentoAssistenza;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppuntamentoAssistenzaRepository extends JpaRepository<AppuntamentoAssistenza, Long> {

    /** Appuntamenti che si sovrappongono all'intervallo [da, a): iniziano prima di "a" e finiscono dopo "da". */
    List<AppuntamentoAssistenza> findByInizioLessThanAndFineGreaterThanOrderByInizioAsc(LocalDateTime a, LocalDateTime da);

    List<AppuntamentoAssistenza> findByDocIdOrderByInizioAsc(Integer docId);
    boolean existsByDocIdAndStato(Integer docId, AppuntamentoAssistenza.Stato stato);
}
