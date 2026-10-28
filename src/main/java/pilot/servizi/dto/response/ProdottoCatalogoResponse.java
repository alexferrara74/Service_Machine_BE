package pilot.servizi.dto.response;

import java.time.LocalDateTime;

public record ProdottoCatalogoResponse(
        Long articoloId,
        String codice,
        String descrizione,
        Integer categoriaId,
        String categoriaDescrizione,
        String classeCodice,
        String sottoclasseCodice,
        Boolean isAttrezzatura,
        LocalDateTime aggiornatoIl
) {}
