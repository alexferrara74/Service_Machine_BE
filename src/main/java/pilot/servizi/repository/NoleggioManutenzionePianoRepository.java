package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.NoleggioManutenzionePiano;

@Repository
public interface NoleggioManutenzionePianoRepository extends JpaRepository<NoleggioManutenzionePiano, Integer> {
}
