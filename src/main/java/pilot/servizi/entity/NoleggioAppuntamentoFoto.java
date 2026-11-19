package pilot.servizi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "noleggio_appuntamento_foto",
        indexes = @Index(name = "idx_appuntamento_foto_appuntamento", columnList = "appuntamento_id")
)
@Getter
@Setter
@NoArgsConstructor
public class NoleggioAppuntamentoFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "appuntamento_id", nullable = false)
    private Long appuntamentoId;

    @Column(name = "nome_file", nullable = false, length = 255)
    private String nomeFile;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Lob
    @Column(name = "contenuto", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenuto;

    @CreationTimestamp
    @Column(name = "creato_il", updatable = false)
    private LocalDateTime creatoIl;
}
