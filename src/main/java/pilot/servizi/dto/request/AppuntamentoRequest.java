package pilot.servizi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Creazione, modifica e chiusura di un appuntamento; tipo ed esito li decide l'operatore. */
public record AppuntamentoRequest(
        @NotNull(message = "La data dell'appuntamento è obbligatoria") LocalDateTime dataPrevista,
        @NotNull(message = "Il tipo (ordinaria o straordinaria) è obbligatorio") String tipo,
        String esito,
        @Size(max = 5000) String resoconto,
        @Min(0) @Max(365) Integer giorniProrogaOrdinaria,
        @Min(0) @Max(365) Integer giorniProrogaStraordinaria
) {}
