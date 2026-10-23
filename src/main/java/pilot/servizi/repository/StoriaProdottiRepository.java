package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.StoriaProdotti;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoriaProdottiRepository extends JpaRepository<StoriaProdotti, Long> {

    List<StoriaProdotti> findByClienteId(Long clienteId);
    List<StoriaProdotti> findByArticoloId(Long articoloId);
    List<StoriaProdotti> findByCodiceArticolo(String codiceArticolo);
    List<StoriaProdotti> findByCategoriaId(Integer categoriaId);
    List<StoriaProdotti> findByAggiornatoIlAfter(LocalDateTime since);
}
