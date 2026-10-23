package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "ricambistica_interventi",
        indexes = {
                @Index(name = "ricambistica_interventi_ibfk_1", columnList = "intervento_id"),
                @Index(name = "idx_ricambi_articolo", columnList = "articolo_id"),
                @Index(name = "idx_ricambi_codice", columnList = "codice_ricambio")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ricambi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "intervento_id", nullable = false)
    private Long interventoId;

    @Column(name = "articolo_id")
    private Long articoloId;

    @Column(name = "codice_ricambio", length = 50)
    private String codiceRicambio;

    @Column(name = "descrizione_ricambio", length = 255)
    private String descrizioneRicambio;

    @Column(name = "quantita_utilizzata")
    private Integer quantitaUtilizzata;

    @Column(name = "prezzo_unitario")
    private Double prezzoUnitario;
}
