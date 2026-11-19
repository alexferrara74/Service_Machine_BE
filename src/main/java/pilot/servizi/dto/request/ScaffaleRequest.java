package pilot.servizi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Misure in centimetri; posizione in pianta riferita all'angolo del magazzino. Il codice, se vuoto, viene generato. */
public record ScaffaleRequest(
        @Size(max = 20) String codice,
        @NotBlank(message = "La zona è obbligatoria") @Size(max = 100) String zona,
        @NotNull @Min(value = 1, message = "La larghezza deve essere maggiore di zero") Integer larghezzaCm,
        @NotNull @Min(value = 1, message = "La profondità deve essere maggiore di zero") Integer profonditaCm,
        @NotNull @Min(value = 1, message = "L'altezza deve essere maggiore di zero") Integer altezzaCm,
        @NotNull @Min(0) Integer posXCm,
        @NotNull @Min(0) Integer posYCm,
        @NotNull Integer rotazioneGradi,
        @NotNull @Min(1) @Max(50) Integer numeroRipiani,
        @NotNull @Min(1) @Max(50) Integer posizioniPerRipiano
) {}
