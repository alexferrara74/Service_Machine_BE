package pilot.servizi.dto.response;

import java.time.LocalDateTime;

public record PosizioneArticoloResponse(
        Long id,
        Long ubicazioneId,
        String codiceUbicazione,
        Long articoloId,
        Integer quantitaPresente,
        LocalDateTime aggiornatoIl
) {}