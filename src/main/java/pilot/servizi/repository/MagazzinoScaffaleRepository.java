package pilot.servizi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.MagazzinoScaffale;

import java.util.List;

@Repository
public interface MagazzinoScaffaleRepository extends JpaRepository<MagazzinoScaffale, Long> {

    boolean existsByCodice(String codice);
    boolean existsByCodiceAndScaffaleIdNot(String codice, Long scaffaleId);
    List<MagazzinoScaffale> findAllByOrderByCodiceAsc();
}
