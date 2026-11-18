package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.ClienteManutenzionePeriodo;

import java.util.List;

@Repository
public interface ClienteManutenzionePeriodoRepository extends JpaRepository<ClienteManutenzionePeriodo, Long> {

    List<ClienteManutenzionePeriodo> findByClienteIdOrderByDataInizioAsc(Long clienteId);

    // Delete in blocco: parte subito. Quello derivato accoda la rimozione e gli INSERT successivi
    // (IDENTITY) la precedono, violando uq_cliente_manutenzione_periodo se una stagione resta uguale.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ClienteManutenzionePeriodo p where p.clienteId = :clienteId")
    void deleteByClienteId(@Param("clienteId") Long clienteId);
}
