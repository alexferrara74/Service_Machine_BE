package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StoriaNoleggioRequest(
        @Size(max = 50) String sezionale,
        @NotNull(message = "La data del noleggio è obbligatoria") LocalDate dataDoc,
        @NotNull(message = "Il cliente è obbligatorio") Long clienteId,
        @NotNull(message = "L'articolo è obbligatorio") Long articoloId,
        @NotBlank(message = "La matricola è obbligatoria")
        @Size(max = 100) String matricola,
        @PositiveOrZero(message = "La quantità non può essere negativa") Double quantita
) {}
