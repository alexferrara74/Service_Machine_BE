package pilot.servizi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Appuntamento di assistenza in calendario. Se {@code docId} è valorizzato riguarda un noleggio censito
 * (cliente e matricola derivano dal noleggio); altrimenti è un cliente non censito inserito a mano
 * e i dati anagrafici sono nelle colonne {@code cliente_*}.
 */
@Entity
@Table(
        name = "appuntamenti_assistenza",
        indexes = {
                @Index(name = "idx_appuntamenti_inizio", columnList = "inizio"),
                @Index(name = "idx_appuntamenti_stato", columnList = "stato"),
                @Index(name = "idx_appuntamenti_doc_id", columnList = "doc_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppuntamentoAssistenza {

    public enum Stato {
        DA_CONFERMARE,
        DA_FARE,
        FATTO,
        ANNULLATO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "doc_id")
    private Integer docId;

    @Column(name = "cliente_nome", length = 255)
    private String clienteNome;

    @Column(name = "cliente_telefono", length = 50)
    private String clienteTelefono;

    @Column(name = "indirizzo", length = 255)
    private String indirizzo;

    @Column(name = "apparecchio", length = 255)
    private String apparecchio;

    @Column(name = "titolo", nullable = false, length = 255)
    private String titolo;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false, length = 20)
    private Stato stato;

    @Column(name = "inizio", nullable = false)
    private LocalDateTime inizio;

    @Column(name = "fine", nullable = false)
    private LocalDateTime fine;

    @Column(name = "tecnico", length = 100)
    private String tecnico;

    /** Intervento effettuato generato alla chiusura (stato FATTO); null finché non è stato chiuso. */
    @Column(name = "intervento_id")
    private Long interventoId;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
