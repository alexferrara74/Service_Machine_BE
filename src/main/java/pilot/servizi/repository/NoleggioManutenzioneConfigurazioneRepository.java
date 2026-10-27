package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.NoleggioManutenzioneConfigurazione;

@Repository
public interface NoleggioManutenzioneConfigurazioneRepository
        extends JpaRepository<NoleggioManutenzioneConfigurazione, Integer> {
}
