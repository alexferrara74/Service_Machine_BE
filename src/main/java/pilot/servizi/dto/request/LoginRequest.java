package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Lo username è obbligatorio") @Size(max = 50) String username,
        @NotBlank(message = "La password è obbligatoria") @Size(max = 200) String password
) {}
