package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.NoleggioAppuntamentoFoto;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoleggioAppuntamentoFotoRepository extends JpaRepository<NoleggioAppuntamentoFoto, Long> {

    List<NoleggioAppuntamentoFoto> findByAppuntamentoIdOrderByIdAsc(Long appuntamentoId);
    Optional<NoleggioAppuntamentoFoto> findByIdAndAppuntamentoId(Long id, Long appuntamentoId);
    long countByAppuntamentoId(Long appuntamentoId);
    void deleteByAppuntamentoId(Long appuntamentoId);
}
