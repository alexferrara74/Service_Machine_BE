package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Table(name = "interventi")
@Getter @Setter @NoArgsConstructor
public class Interventi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long interventoId;
    private Long clienteId;
    private String matricolaAttrezzatura;
    private String titolo;
    private String descrizioneProblema;
    private String soluzioneApplicata;
    private String stato;
    private LocalDateTime dataApertura;
    private LocalDateTime dataChiusura;
    private Double costoManodopera;
}