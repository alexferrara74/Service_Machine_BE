package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.NoleggioManutenzione;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoleggioManutenzioneRepository extends JpaRepository<NoleggioManutenzione, Long> {

    List<NoleggioManutenzione> findByDocIdOrderByIdAsc(Integer docId);

    Optional<NoleggioManutenzione> findFirstByDocIdOrderByIdDesc(Integer docId);

    Optional<NoleggioManutenzione> findFirstByDocIdAndTipoOrderByIdDesc(
            Integer docId,
            NoleggioManutenzione.Tipo tipo
    );

    long countByDocIdAndTipoAndIdGreaterThan(
            Integer docId,
            NoleggioManutenzione.Tipo tipo,
            Long id
    );
}
