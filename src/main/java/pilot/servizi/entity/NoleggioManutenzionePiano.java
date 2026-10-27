package pilot.servizi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "noleggio_manutenzione_piani")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoleggioManutenzionePiano {

    @Id
    @Column(name = "doc_id", nullable = false, updatable = false)
    private Integer docId;

    @Column(name = "giorni_avviso", nullable = false)
    private Integer giorniAvviso;
}
