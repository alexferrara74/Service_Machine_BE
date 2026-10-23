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
        name = "local_attrezzature",
        uniqueConstraints = {
                @UniqueConstraint(name = "matricola", columnNames = {"matricola"})
        },
        indexes = {
                @Index(name = "idx_attrezzature_stato", columnList = "stato"),
                @Index(name = "idx_attrezzature_marca", columnList = "marca")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attrezzature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "matricola", length = 100, unique = true)
    private String matricola;

    @Column(name = "descrizione", length = 255)
    private String descrizione;

    @Column(name = "modello", length = 100)
    private String modello;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "stato", length = 50)
    private String stato;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
