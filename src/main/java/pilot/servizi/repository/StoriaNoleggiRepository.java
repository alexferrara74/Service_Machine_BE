package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.StoriaNoleggi;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoriaNoleggiRepository extends JpaRepository<StoriaNoleggi, Integer> {

    List<StoriaNoleggi> findByClienteId(Long clienteId);
    List<StoriaNoleggi> findByArticoloId(Long articoloId);
    List<StoriaNoleggi> findByMatricola(String matricola);
    List<StoriaNoleggi> findByAggiornatoIlAfter(LocalDateTime since);
}
