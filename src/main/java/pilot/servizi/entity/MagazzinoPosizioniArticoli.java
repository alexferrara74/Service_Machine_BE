package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



import java.time.LocalDateTime;

@Entity
@Table(name = "magazzino_posizioni_articoli")
@Getter
@Setter
@NoArgsConstructor
public class MagazzinoPosizioniArticoli {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ubicazioneId;
    private Long articoloId;
    private Integer quantitaPresente;
    private LocalDateTime aggiornatoIl;
}