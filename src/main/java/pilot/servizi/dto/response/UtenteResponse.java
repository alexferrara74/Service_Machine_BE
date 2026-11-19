package pilot.servizi.dto.response;

import pilot.servizi.entity.Utente;

import java.time.LocalDateTime;

public record UtenteResponse(
        Long id,
        String username,
        Utente.Ruolo ruolo,
        boolean attivo,
        LocalDateTime ultimoAccesso,
        LocalDateTime creatoIl
) {}
