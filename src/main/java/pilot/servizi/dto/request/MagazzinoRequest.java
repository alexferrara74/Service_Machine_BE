package pilot.servizi.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record MagazzinoRequest(
        @NotBlank(message = "Il codice ubicazione è obbligatorio") @Size(max = 50) String codiceUbicazione,
        @NotBlank(message = "La zona è obbligatoria") @Size(max = 50) String zona,
        @Size(max = 50) String scaffale,
        @Size(max = 50) String ripiano,
        @Min(value = 1, message = "La capacità massima deve essere almeno 1") Integer capacitaMax,
        @Size(max = 500) String note
) {}