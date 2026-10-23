package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PosizioneArticoloRequest(
        @NotNull(message = "L'ID ubicazione è obbligatorio") Long ubicazioneId,
        @NotNull(message = "L'ID articolo è obbligatorio") Long articoloId,
        @NotNull(message = "La quantità presente è obbligatoria") @PositiveOrZero(message = "La quantità non può essere negativa") Integer quantitaPresente
) {}
