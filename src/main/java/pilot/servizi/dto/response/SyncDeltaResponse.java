package pilot.servizi.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SyncDeltaResponse(
        LocalDateTime since,
        List<ClienteResponse> clienti,
        List<InterventoResponse> interventi
) {}