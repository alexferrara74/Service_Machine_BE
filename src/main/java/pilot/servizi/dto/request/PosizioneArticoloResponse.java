package pilot.servizi.dto.request;

import java.time.LocalDateTime;

public record PosizioneArticoloResponse(
        Long id,
        Long ubicazioneId,
        String codiceUbicazione,
        Long articoloId,
        Integer quantitaPresente,
        LocalDateTime aggiornatoIl
) {}