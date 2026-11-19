package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pilot.servizi.entity.Utente;

public record UtenteRequest(
        @NotBlank(message = "Lo username è obbligatorio") @Size(max = 50) String username,
        @NotBlank(message = "La password è obbligatoria") @Size(max = 200) String password,
        @NotNull(message = "Il ruolo è obbligatorio") Utente.Ruolo ruolo
) {}
