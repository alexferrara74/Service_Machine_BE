package pilot.servizi.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StoriaProdottoResponse(
        Long id,
        Long clienteId,
        Long articoloId,
        String codiceArticolo,
        String descrizione,
        Integer categoriaId,
        String categoriaDescrizione,
        Double quantita,
        LocalDate dataDoc,
        LocalDateTime aggiornatoIl
) {}
