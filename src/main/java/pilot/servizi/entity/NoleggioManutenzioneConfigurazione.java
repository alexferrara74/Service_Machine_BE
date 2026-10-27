package pilot.servizi.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "noleggio_manutenzione_configurazioni")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoleggioManutenzioneConfigurazione {

    @Id
    @Column(name = "doc_id", nullable = false, updatable = false)
    private Integer docId;

    @Column(name = "soglia_quantita", nullable = false)
    private Double sogliaQuantita;

    @Column(name = "ordinarie_per_straordinaria", nullable = false)
    private Integer ordinariePerStraordinaria;

    @ElementCollection
    @CollectionTable(
            name = "noleggio_manutenzione_categorie",
            joinColumns = @JoinColumn(name = "doc_id"),
            indexes = @Index(name = "idx_noleggio_manutenzione_categoria", columnList = "categoria_id")
    )
    @Column(name = "categoria_id", nullable = false)
    @Builder.Default
    private Set<Integer> categoriaIds = new HashSet<>();
}
