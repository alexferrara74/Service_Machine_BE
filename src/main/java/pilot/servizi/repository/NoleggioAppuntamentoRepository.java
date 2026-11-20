package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.NoleggioAppuntamento;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoleggioAppuntamentoRepository extends JpaRepository<NoleggioAppuntamento, Long> {

    List<NoleggioAppuntamento> findByDocIdOrderByDataPrevistaDesc(Integer docId);
    Optional<NoleggioAppuntamento> findByIdAndDocId(Long id, Integer docId);
    boolean existsByDocIdAndStato(Integer docId, NoleggioAppuntamento.Stato stato);
    List<NoleggioAppuntamento> findByStatoOrderByDataPrevistaAsc(NoleggioAppuntamento.Stato stato);
    List<NoleggioAppuntamento> findAllByOrderByDataPrevistaDesc();
}
