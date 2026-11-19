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
        name = "noleggio_appuntamenti",
        indexes = {
                @Index(name = "idx_appuntamenti_doc_id", columnList = "doc_id"),
                @Index(name = "idx_appuntamenti_stato_data", columnList = "stato, data_prevista")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoleggioAppuntamento {

    public enum Stato { PROGRAMMATO, COMPLETATO, ANNULLATO }

    public enum Esito { POSITIVO, NEGATIVO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "doc_id", nullable = false)
    private Integer docId;

    @Column(name = "data_prevista", nullable = false)
    private LocalDateTime dataPrevista;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false, length = 20)
    private Stato stato;

    /** Decisa dall'operatore: ordinaria o straordinaria. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private NoleggioManutenzione.Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "esito", length = 20)
    private Esito esito;

    @Column(name = "resoconto", columnDefinition = "TEXT")
    private String resoconto;

    /** Giorni di cui viene allungata la prossima scadenza quando l'esito è positivo. */
    @Column(name = "giorni_proroga")
    private Integer giorniProroga;

    @Column(name = "completato_il")
    private LocalDateTime completatoIl;

    @Column(name = "manutenzione_id")
    private Long manutenzioneId;

    @CreationTimestamp
    @Column(name = "creato_il", updatable = false)
    private LocalDateTime creatoIl;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
