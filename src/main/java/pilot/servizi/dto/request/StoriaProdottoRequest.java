package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StoriaProdottoRequest(
        Long clienteId,
        Long articoloId,
        @NotBlank(message = "Il codice articolo è obbligatorio") @Size(max = 50) String codiceArticolo,
        @Size(max = 255) String descrizione,
        Integer categoriaId,
        @Size(max = 255) String categoriaDescrizione,
        @NotNull(message = "La quantità è obbligatoria") @PositiveOrZero(message = "La quantità non può essere negativa") Double quantita,
        LocalDate dataDoc
) {}
