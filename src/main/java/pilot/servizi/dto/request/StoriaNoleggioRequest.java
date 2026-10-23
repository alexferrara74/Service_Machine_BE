package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StoriaNoleggioRequest(
        @NotNull(message = "L'ID documento è obbligatorio") Integer docId,
        @Size(max = 50) String sezionale,
        LocalDate dataDoc,
        Long clienteId,
        Long articoloId,
        @Size(max = 100) String matricola,
        @PositiveOrZero(message = "La quantità non può essere negativa") Double quantita
) {}
