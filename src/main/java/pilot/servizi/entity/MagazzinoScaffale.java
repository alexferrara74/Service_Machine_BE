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
        name = "magazzino_scaffali",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_magazzino_scaffale_codice", columnNames = {"codice"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MagazzinoScaffale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scaffale_id", nullable = false, updatable = false)
    private Long scaffaleId;

    @Column(name = "codice", nullable = false, length = 30)
    private String codice;

    @Column(name = "zona", nullable = false, length = 100)
    private String zona;

    @Column(name = "larghezza_cm", nullable = false)
    private Integer larghezzaCm;

    @Column(name = "profondita_cm", nullable = false)
    private Integer profonditaCm;

    @Column(name = "altezza_cm", nullable = false)
    private Integer altezzaCm;

    @Column(name = "pos_x_cm", nullable = false)
    private Integer posXCm;

    @Column(name = "pos_y_cm", nullable = false)
    private Integer posYCm;

    @Column(name = "rotazione_gradi", nullable = false)
    private Integer rotazioneGradi;

    @Column(name = "numero_ripiani", nullable = false)
    private Integer numeroRipiani;

    @Column(name = "posizioni_per_ripiano", nullable = false)
    private Integer posizioniPerRipiano;

    @CreationTimestamp
    @Column(name = "creato_il", updatable = false)
    private LocalDateTime creatoIl;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
