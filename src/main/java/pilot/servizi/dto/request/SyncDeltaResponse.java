package pilot.servizi.dto.request;

import pilot.servizi.dto.response.ClienteResponse;
import pilot.servizi.dto.response.InterventoResponse;

import java.time.LocalDateTime;
import java.util.List;

public record SyncDeltaResponse(
        LocalDateTime since,
        List<ClienteResponse> clienti,
        List<InterventoResponse> interventi
) {}