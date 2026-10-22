package pilot.servizi.dto.response;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String codice,
        String ragioneSociale,
        String nome,
        String cognome,
        String telefono,
        String email,
        String partitaIva,
        String codiceFiscale,
        Integer tipoListino,
        LocalDateTime aggiornatoIl
) {}