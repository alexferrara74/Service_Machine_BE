package pilot.servizi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ClienteRequest(
        @NotBlank(message = "Il codice è obbligatorio") @Size(max = 50) String codice,
        @NotBlank(message = "La ragione sociale è obbligatoria") @Size(max = 255) String ragioneSociale,
        @Size(max = 100) String nome,
        @Size(max = 100) String cognome,
        @Size(max = 30) String telefono,
        @Email(message = "Email non valida") @Size(max = 255) String email,
        @Size(max = 20) String partitaIva,
        @Size(max = 20) String codiceFiscale,
        Integer tipoListino
) {}