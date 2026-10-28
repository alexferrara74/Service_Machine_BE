package pilot.servizi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "local_prodotti",
        indexes = {
                @Index(name = "idx_local_prodotti_categoria", columnList = "categoria_id"),
                @Index(name = "idx_local_prodotti_attrezzatura", columnList = "is_attrezzatura")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ProdottiCatalogo {

    @Id
    @Column(name = "articolo_id", nullable = false, updatable = false)
    private Long articoloId;

    @Column(name = "codice", nullable = false, length = 50)
    private String codice;

    @Column(name = "descrizione", nullable = false, length = 255)
    private String descrizione;

    @Column(name = "categoria_id")
    private Integer categoriaId;

    @Column(name = "categoria_descrizione", length = 255)
    private String categoriaDescrizione;

    @Column(name = "classe_codice", length = 10)
    private String classeCodice;

    @Column(name = "sottoclasse_codice", length = 10)
    private String sottoclasseCodice;

    @Column(name = "is_attrezzatura", nullable = false)
    private Boolean isAttrezzatura;

    @UpdateTimestamp
    @Column(name = "aggiornato_il")
    private LocalDateTime aggiornatoIl;
}
