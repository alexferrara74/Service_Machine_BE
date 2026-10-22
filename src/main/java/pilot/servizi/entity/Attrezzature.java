package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "local_attrezzature")
@Getter
@Setter
@NoArgsConstructor
public class Attrezzature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matricola;
    private String descrizione;
    private String modello;
    private String marca;
    private String stato;
    private LocalDateTime aggiornatoIl;
}