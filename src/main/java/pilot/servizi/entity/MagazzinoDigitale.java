package pilot.servizi.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Table(name = "magazzino_digitale")
@Getter @Setter @NoArgsConstructor
public class MagazzinoDigitale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ubicazioneId;

    private String codiceUbicazione;
    private String zona;
    private String scaffale;
    private String ripiano;
    private Integer capacitaMax;
    private String note;
    private LocalDateTime creatoIl;
    private LocalDateTime aggiornatoIl;
}