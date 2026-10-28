package pilot.servizi.dto.response;

import java.time.LocalDateTime;

public record DocumentoNoleggioResponse(
        String nomeFile,
        LocalDateTime aggiornatoIl
) {}
