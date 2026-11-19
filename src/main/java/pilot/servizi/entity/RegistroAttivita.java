package pilot.servizi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

/** Una riga del registro: immutabile, non ha setter usati dopo la creazione né endpoint di modifica. */
@Entity
@Table(
        name = "registro_attivita",
        indexes = {
                @Index(name = "idx_registro_quando", columnList = "quando"),
                @Index(name = "idx_registro_username", columnList = "username"),
                @Index(name = "idx_registro_azione", columnList = "azione")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroAttivita {

    public enum Azione {
        LOGIN,
        LOGIN_FALLITO,
        LOGIN_BLOCCATO,
        CAMBIO_PASSWORD,
        CREA,
        MODIFICA,
        ELIMINA,
        ESPORTA
    }

    /** OK = eseguita; NEGATO = 401/403; RIFIUTATO = richiesta non valida o in conflitto (4xx); ERRORE = 5xx. */
    public enum Esito {
        OK,
        NEGATO,
        RIFIUTATO,
        ERRORE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "quando", nullable = false, updatable = false)
    private LocalDateTime quando;

    @Column(name = "username", length = 50, updatable = false)
    private String username;

    @Column(name = "ruolo", length = 20, updatable = false)
    private String ruolo;

    @Column(name = "azione", nullable = false, length = 30, updatable = false)
    private String azione;

    @Column(name = "risorsa", length = 80, updatable = false)
    private String risorsa;

    @Column(name = "risorsa_id", length = 40, updatable = false)
    private String risorsaId;

    @Column(name = "esito", nullable = false, length = 15, updatable = false)
    private String esito;

    @Column(name = "codice_http", updatable = false)
    private Integer codiceHttp;

    @Column(name = "ip", length = 45, updatable = false)
    private String ip;

    @Column(name = "dettaglio", length = 255, updatable = false)
    private String dettaglio;
}
