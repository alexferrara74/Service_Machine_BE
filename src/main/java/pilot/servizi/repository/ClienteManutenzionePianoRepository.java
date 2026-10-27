package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.ClienteManutenzionePiano;

@Repository
public interface ClienteManutenzionePianoRepository extends JpaRepository<ClienteManutenzionePiano, Long> {
}
