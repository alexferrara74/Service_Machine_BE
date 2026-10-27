package pilot.servizi.dto.request;

import jakarta.validation.constraints.Size;

public record NoleggioManutenzioneRequest(
        @Size(max = 2000) String note
) {
}
