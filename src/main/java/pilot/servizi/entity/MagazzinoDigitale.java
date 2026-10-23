package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "magazzino_digitale",
        uniqueConstraints = {
                @UniqueConstraint(name = "codice_ubicazione", columnNames = {"codice_ubicazione"})
        },
        indexes = {
                @Index(name = "idx_magazzino_zona_scaffale", columnList = "zona, scaffale")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MagazzinoDigitale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ubicazione_id", nullable = false, updatable = false)
    private Long ubicazioneId;

    @Column(name = "codice_ubicazione", length = 50, unique = true)
    private String codiceUbicazione;

    @Column(name = "zona", length = 100)
    private String zona;

    @Column(name = "scaffale", length = 50)
    private String scaffale;

    @Column(name = "ripiano", length = 50)
    private String ripiano;

    @Column(name = "capacita_max")
    private Integer capacitaMax;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "creato_il", updatable = false)
    private LocalDateTime creatoIl;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
