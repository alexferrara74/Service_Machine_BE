package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.StoriaNoleggioDocumento;

@Repository
public interface StoriaNoleggioDocumentoRepository extends JpaRepository<StoriaNoleggioDocumento, Integer> {}
