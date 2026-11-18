package pilot.servizi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pilot.servizi.entity.ClienteManutenzionePeriodo;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ClienteManutenzionePeriodoRepositoryTest {

    @Autowired
    private ClienteManutenzionePeriodoRepository repository;

    private ClienteManutenzionePeriodo estate(Long clienteId, int intervallo) {
        return ClienteManutenzionePeriodo.builder()
                .clienteId(clienteId)
                .dataInizio(LocalDate.of(2026, 6, 1))
                .dataFine(LocalDate.of(2026, 9, 30))
                .intervalloGiorni(intervallo)
                .build();
    }

    @Test
    void replacesThePeriodsEvenWhenASeasonKeepsTheSameDates() {
        repository.save(estate(1L, 30));
        repository.save(estate(2L, 60));

        repository.deleteByClienteId(1L);
        repository.saveAll(List.of(estate(1L, 45)));
        repository.flush();

        assertEquals(45, repository.findByClienteIdOrderByDataInizioAsc(1L).get(0).getIntervalloGiorni());
        assertEquals(60, repository.findByClienteIdOrderByDataInizioAsc(2L).get(0).getIntervalloGiorni());
    }
}
