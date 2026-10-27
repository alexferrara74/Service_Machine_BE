package pilot.servizi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "noleggio_manutenzioni",
        indexes = @Index(name = "idx_noleggio_manutenzioni_doc_id_id", columnList = "doc_id, id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoleggioManutenzione {

    public enum Tipo {
        ORDINARIA,
        STRAORDINARIA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "doc_id", nullable = false)
    private Integer docId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private Tipo tipo;

    @Column(name = "quantita_acquistata_totale", nullable = false)
    private Double quantitaAcquistataTotale;

    @Column(name = "eseguita_il", nullable = false)
    private LocalDateTime eseguitaIl;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
