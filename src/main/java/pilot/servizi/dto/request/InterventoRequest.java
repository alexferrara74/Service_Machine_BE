package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record InterventoRequest(
        @NotNull(message = "Il cliente è obbligatorio") Long clienteId,
        @Size(max = 50) String matricolaAttrezzatura,
        @NotBlank(message = "Il titolo è obbligatorio") @Size(max = 255) String titolo,
        @Size(max = 2000) String descrizioneProblema,
        @Size(max = 2000) String soluzioneApplicata,
        @NotBlank(message = "Lo stato è obbligatorio") @Size(max = 50) String stato,
        @NotNull(message = "La data di apertura è obbligatoria") LocalDateTime dataApertura,
        LocalDateTime dataChiusura,
        @PositiveOrZero(message = "Il costo non può essere negativo") Double costoManodopera
) {}