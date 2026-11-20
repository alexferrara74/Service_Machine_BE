package pilot.servizi.dto.response;

import java.time.LocalDateTime;

public record AttrezzaturaResponse(
        Long id,
        String matricola,
        String descrizione,
        String modello,
        String marca,
        String stato,
        String origine,
        LocalDateTime aggiornatoIl,
        Long articoloId
) {}