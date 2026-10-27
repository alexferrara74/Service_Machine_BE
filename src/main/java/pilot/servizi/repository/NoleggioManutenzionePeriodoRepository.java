package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.NoleggioManutenzionePeriodo;

import java.util.List;

@Repository
public interface NoleggioManutenzionePeriodoRepository extends JpaRepository<NoleggioManutenzionePeriodo, Long> {

    List<NoleggioManutenzionePeriodo> findByDocIdOrderByDataInizioAsc(Integer docId);

    void deleteByDocId(Integer docId);
}
