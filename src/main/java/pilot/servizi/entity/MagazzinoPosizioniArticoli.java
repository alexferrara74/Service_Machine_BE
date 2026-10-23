package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "magazzino_posizioni_articoli",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_art_pos", columnNames = {"ubicazione_id", "articolo_id"})
        },
        indexes = {
                @Index(name = "idx_pos_articolo", columnList = "articolo_id"),
                @Index(name = "idx_pos_ubicazione", columnList = "ubicazione_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MagazzinoPosizioniArticoli {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "ubicazione_id", nullable = false)
    private Long ubicazioneId;

    @Column(name = "articolo_id", nullable = false)
    private Long articoloId;

    @Column(name = "quantita_presente")
    private Integer quantitaPresente;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
