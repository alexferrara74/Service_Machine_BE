package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequest(
        @NotBlank(message = "La password attuale è obbligatoria") @Size(max = 200) String passwordAttuale,
        @NotBlank(message = "La nuova password è obbligatoria") @Size(max = 200) String nuovaPassword
) {}
