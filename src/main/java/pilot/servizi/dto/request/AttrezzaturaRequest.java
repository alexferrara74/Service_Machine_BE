package pilot.servizi.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AttrezzaturaRequest(
        @NotBlank(message = "La matricola è obbligatoria") @Size(max = 100) String matricola,
        @Size(max = 255) String descrizione,
        @Size(max = 100) String modello,
        @Size(max = 100) String marca,
        @Size(max = 50) String stato
) {}