package pilot.servizi.dto.response;


import java.time.LocalDateTime;

public record MagazzinoResponse(
        Long ubicazioneId,
        String codiceUbicazione,
        String zona,
        String scaffale,
        String ripiano,
        Integer capacitaMax,
        String note,
        LocalDateTime creatoIl,
        LocalDateTime aggiornatoIl
) {}