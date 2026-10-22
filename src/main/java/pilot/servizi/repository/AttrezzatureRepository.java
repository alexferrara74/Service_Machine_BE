package pilot.servizi.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pilot.servizi.entity.Attrezzature;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttrezzatureRepository extends JpaRepository<Attrezzature, Long> {

    Optional<Attrezzature> findByMatricola(String matricola);
    boolean existsByMatricola(String matricola);

    List<Attrezzature> findByStato(String stato);
    List<Attrezzature> findByMarcaIgnoreCase(String marca);
    List<Attrezzature> findByDescrizioneContainingIgnoreCase(String descrizione);

    List<Attrezzature> findByAggiornatoIlAfter(LocalDateTime since);
}