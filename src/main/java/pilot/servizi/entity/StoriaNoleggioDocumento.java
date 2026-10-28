package pilot.servizi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "storia_noleggi_documenti")
@Getter
@Setter
@NoArgsConstructor
public class StoriaNoleggioDocumento {

    @Id
    @Column(name = "doc_id", nullable = false, updatable = false)
    private Integer docId;

    @Column(name = "nome_file", nullable = false, length = 255)
    private String nomeFile;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Lob
    @Column(name = "contenuto", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenuto;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
