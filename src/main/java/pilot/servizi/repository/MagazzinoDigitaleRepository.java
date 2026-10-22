package pilot.servizi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.MagazzinoDigitale;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MagazzinoDigitaleRepository extends JpaRepository<MagazzinoDigitale, Long> {

    Optional<MagazzinoDigitale> findByCodiceUbicazione(String codiceUbicazione);
    boolean existsByCodiceUbicazione(String codiceUbicazione);

    List<MagazzinoDigitale> findByZona(String zona);
    List<MagazzinoDigitale> findByZonaAndScaffale(String zona, String scaffale);

    List<MagazzinoDigitale> findByAggiornatoIlAfter(LocalDateTime since);
}