package pilot.servizi.dto.response;

import java.time.LocalDateTime;

public record NoleggioManutenzioneEventoResponse(
        Long id,
        Integer docId,
        String tipo,
        Double quantitaAcquistataTotale,
        LocalDateTime eseguitaIl,
        String note
) {
}
