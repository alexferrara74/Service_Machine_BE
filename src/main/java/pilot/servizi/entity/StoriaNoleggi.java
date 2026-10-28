package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.generator.EventType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "storia_noleggi",
        indexes = {
                @Index(name = "idx_storia_noleggi_cliente", columnList = "cliente_id"),
                @Index(name = "idx_storia_noleggi_articolo", columnList = "articolo_id"),
                @Index(name = "idx_storia_noleggi_matricola", columnList = "matricola")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoriaNoleggi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id", nullable = false)
    private Integer docId;

    @Column(name = "sezionale", length = 50)
    private String sezionale;

    @Column(name = "data_doc")
    private LocalDate dataDoc;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "articolo_id")
    private Long articoloId;

    @Column(name = "matricola", length = 100)
    private String matricola;

    @Column(name = "quantita")
    private Double quantita;

    @Column(name = "codice_articolo", length = 50)
    private String codiceArticolo;

    @Column(name = "descrizione", length = 255)
    private String descrizione;

    /** ERP se la riga arriva dal sync, MANUALE se inserita dall'operatore (default del DB). */
    @Generated(event = EventType.INSERT)
    @Column(name = "origine", length = 10, insertable = false, updatable = false)
    private String origine;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
