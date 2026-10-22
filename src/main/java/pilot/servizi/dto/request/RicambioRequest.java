package pilot.servizi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record RicambioRequest(
        @NotNull(message = "L'intervento è obbligatorio") Long interventoId,
        @NotNull(message = "L'articolo è obbligatorio") Long articoloId,
        @Size(max = 50) String codiceRicambio,
        @Size(max = 255) String descrizioneRicambio,
        @NotNull(message = "La quantità è obbligatoria") @Min(value = 1, message = "La quantità deve essere almeno 1") Integer quantitaUtilizzata,
        @PositiveOrZero(message = "Il prezzo non può essere negativo") Double prezzoUnitario
) {}