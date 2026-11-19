package pilot.servizi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "utenti", uniqueConstraints = @UniqueConstraint(name = "uq_utenti_username", columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utente {

    /**
     * ADMIN può fare tutto; VIEWER può solo leggere; TECNICO legge tutto e, negli appuntamenti,
     * conferma, chiude (non annulla), scrive resoconto/note e allega foto.
     */
    public enum Ruolo {
        ADMIN,
        TECNICO,
        VIEWER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "ruolo", nullable = false, length = 20)
    private Ruolo ruolo;

    @Column(name = "attivo", nullable = false)
    private boolean attivo;

    /** I token emessi prima di questo istante non sono più validi (cambio o reset password). */
    @Column(name = "password_modificata_il", nullable = false)
    private LocalDateTime passwordModificataIl;

    @Column(name = "ultimo_accesso")
    private LocalDateTime ultimoAccesso;

    @Column(name = "creato_il", nullable = false, updatable = false)
    private LocalDateTime creatoIl;
}
