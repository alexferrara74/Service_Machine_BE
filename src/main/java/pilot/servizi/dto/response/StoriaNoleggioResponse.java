package pilot.servizi.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StoriaNoleggioResponse(
        Integer docId,
        String sezionale,
        LocalDate dataDoc,
        Long clienteId,
        Long articoloId,
        String matricola,
        Double quantita,
        String codiceArticolo,
        String descrizione,
        String origine,
        LocalDateTime aggiornatoIl
) {}
