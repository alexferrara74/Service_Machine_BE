package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ricambistica_interventi")
@Getter @Setter @NoArgsConstructor
public class Ricambi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long interventoId;
    private Long articoloId;
    private String codiceRicambio;
    private String descrizioneRicambio;
    private Integer quantitaUtilizzata;
    private Double prezzoUnitario;
}