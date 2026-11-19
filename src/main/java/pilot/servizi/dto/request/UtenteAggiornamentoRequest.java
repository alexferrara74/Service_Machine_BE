package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pilot.servizi.entity.Utente;

/** {@code nuovaPassword} vuota = la password resta quella attuale; valorizzata = reset da parte dell'admin. */
public record UtenteAggiornamentoRequest(
        @NotNull(message = "Il ruolo è obbligatorio") Utente.Ruolo ruolo,
        @NotNull(message = "Lo stato attivo/disattivo è obbligatorio") Boolean attivo,
        @Size(max = 200) String nuovaPassword
) {}
