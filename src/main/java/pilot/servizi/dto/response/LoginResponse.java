package pilot.servizi.dto.response;

import pilot.servizi.entity.Utente;

import java.time.Instant;

public record LoginResponse(String token, Instant scadenza, String username, Utente.Ruolo ruolo) {}
