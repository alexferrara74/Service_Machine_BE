package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record NoleggioManutenzioneConfigurazioneRequest(
        Double sogliaQuantita,
        Integer ordinariePerStraordinaria,
        @NotEmpty Set<@NotNull @Positive Integer> categoriaIds
) {
}
