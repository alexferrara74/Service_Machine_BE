package pilot.servizi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "noleggio_manutenzione_periodi",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_manutenzione_periodo",
                columnNames = {"doc_id", "data_inizio", "data_fine"}
        ),
        indexes = @Index(name = "idx_manutenzione_periodo_date", columnList = "doc_id, data_inizio, data_fine")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoleggioManutenzionePeriodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "doc_id", nullable = false)
    private Integer docId;

    @Column(name = "data_inizio", nullable = false)
    private LocalDate dataInizio;

    @Column(name = "data_fine", nullable = false)
    private LocalDate dataFine;

    @Column(name = "intervallo_giorni", nullable = false)
    private Integer intervalloGiorni;
}
