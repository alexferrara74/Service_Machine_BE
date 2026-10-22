package pilot.servizi.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Table(name = "local_clienti")
@Getter @Setter @NoArgsConstructor
public class Clienti {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codice;
    private String ragioneSociale;
    private String nome;
    private String cognome;
    private String telefono;
    private String email;
    private String partitaIva;
    private String codiceFiscale;
    private Integer tipoListino;
    private LocalDateTime aggiornatoIl;
}