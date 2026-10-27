package pilot.servizi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.StoriaNoleggi;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoriaNoleggiRepository extends JpaRepository<StoriaNoleggi, Integer> {

    List<StoriaNoleggi> findByClienteId(Long clienteId);
    Page<StoriaNoleggi> findByClienteId(Long clienteId, Pageable pageable);
    List<StoriaNoleggi> findByArticoloId(Long articoloId);
    Page<StoriaNoleggi> findByArticoloId(Long articoloId, Pageable pageable);
    List<StoriaNoleggi> findByMatricola(String matricola);
    Page<StoriaNoleggi> findByMatricola(String matricola, Pageable pageable);
    List<StoriaNoleggi> findByAggiornatoIlAfter(LocalDateTime since);
}
