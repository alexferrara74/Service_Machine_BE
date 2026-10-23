package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "storia_prodotti",
        indexes = {
                @Index(name = "idx_storia_prodotti_cliente", columnList = "cliente_id"),
                @Index(name = "idx_storia_prodotti_articolo", columnList = "articolo_id"),
                @Index(name = "idx_storia_prodotti_codice", columnList = "codice_articolo")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoriaProdotti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "articolo_id")
    private Long articoloId;

    @Column(name = "codice_articolo", length = 50)
    private String codiceArticolo;

    @Column(name = "descrizione", length = 255)
    private String descrizione;

    @Column(name = "categoria_id")
    private Integer categoriaId;

    @Column(name = "categoria_descrizione", length = 255)
    private String categoriaDescrizione;

    @Column(name = "quantita")
    private Double quantita;

    @Column(name = "data_doc")
    private LocalDate dataDoc;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
