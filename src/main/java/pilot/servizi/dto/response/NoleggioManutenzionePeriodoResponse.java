package pilot.servizi.dto.response;

import java.time.LocalDate;

public record NoleggioManutenzionePeriodoResponse(
        LocalDate dataInizio,
        LocalDate dataFine,
        Integer intervalloGiorni
) {
}
