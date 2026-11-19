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
        name = "local_clienti",
        indexes = {
                @Index(name = "idx_clienti_codice", columnList = "codice"),
                @Index(name = "idx_clienti_ragione_sociale", columnList = "ragione_sociale"),
                @Index(name = "idx_clienti_piva", columnList = "partita_iva"),
                @Index(name = "idx_clienti_cf", columnList = "codice_fiscale")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clienti {

    /** Per i clienti ERP è l'id dell'ERP; per quelli manuali lo assegna il service (vedi Origini): la colonna non è auto-increment. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "codice", length = 50)
    private String codice;

    @Column(name = "ragione_sociale", length = 255)
    private String ragioneSociale;

    @Column(name = "nome", length = 100)
    private String nome;

    @Column(name = "cognome", length = 100)
    private String cognome;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "partita_iva", length = 20)
    private String partitaIva;

    @Column(name = "codice_fiscale", length = 20)
    private String codiceFiscale;

    @Column(name = "tipo_listino")
    private Integer tipoListino;

    /** ERP se importato dal sync, MANUALE se creato dall'operatore. */
    @Column(name = "origine", length = 10, updatable = false)
    private String origine;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
