package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.ClienteManutenzionePeriodo;

import java.util.List;

@Repository
public interface ClienteManutenzionePeriodoRepository extends JpaRepository<ClienteManutenzionePeriodo, Long> {

    List<ClienteManutenzionePeriodo> findByClienteIdOrderByDataInizioAsc(Long clienteId);

    void deleteByClienteId(Long clienteId);
}
