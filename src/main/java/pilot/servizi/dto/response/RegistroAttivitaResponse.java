package pilot.servizi.dto.response;

import java.time.LocalDateTime;

public record RegistroAttivitaResponse(
        Long id,
        LocalDateTime quando,
        String username,
        String ruolo,
        String azione,
        String risorsa,
        String risorsaId,
        String esito,
        Integer codiceHttp,
        String ip,
        String dettaglio
) {}
