package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "interventi",
        indexes = {
                @Index(name = "interventi_ibfk_1", columnList = "cliente_id"),
                @Index(name = "interventi_ibfk_2", columnList = "matricola_attrezzatura"),
                @Index(name = "idx_interventi_stato", columnList = "stato"),
                @Index(name = "idx_interventi_data_apertura", columnList = "data_apertura")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interventi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intervento_id", nullable = false, updatable = false)
    private Long interventoId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "matricola_attrezzatura", nullable = false, length = 100)
    private String matricolaAttrezzatura;

    @Column(name = "titolo", length = 255)
    private String titolo;

    @Column(name = "descrizione_problema", columnDefinition = "TEXT")
    private String descrizioneProblema;

    @Column(name = "soluzione_applicata", columnDefinition = "TEXT")
    private String soluzioneApplicata;

    @Column(name = "stato", length = 50)
    private String stato;

    @Column(name = "data_apertura")
    private LocalDateTime dataApertura;

    @Column(name = "data_chiusura")
    private LocalDateTime dataChiusura;

    @Column(name = "costo_manodopera", precision = 10, scale = 2)
    private BigDecimal costoManodopera;
}
