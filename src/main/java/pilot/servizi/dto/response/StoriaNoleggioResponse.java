package pilot.servizi.dto.response;

import java.time.LocalDateTime;

public record StoriaNoleggioResponse(
        Long docId,
        String sezionale,
        LocalDateTime dataDoc,
        Long clienteId,
        Long articoloId,
        String matricola,
        Integer quantita,
        LocalDateTime aggiornatoIl
) {}