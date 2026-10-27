package pilot.servizi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record NoleggioManutenzionePianificazioneRequest(
        @NotNull @Min(0) Integer giorniAvviso,
        @NotEmpty List<@Valid PeriodoRequest> periodi
) {
    public record PeriodoRequest(
            @NotNull LocalDate dataInizio,
            @NotNull LocalDate dataFine,
            @NotNull @Positive Integer intervalloGiorni
    ) {
    }
}
