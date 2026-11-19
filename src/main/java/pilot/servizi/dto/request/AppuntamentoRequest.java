package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pilot.servizi.entity.AppuntamentoAssistenza;

import java.time.LocalDateTime;

/**
 * Con {@code docId} l'appuntamento è su un noleggio censito; senza, {@code clienteNome} è obbligatorio
 * (cliente non censito, caso eccezionale).
 */
public record AppuntamentoRequest(
        Integer docId,
        @Size(max = 255) String clienteNome,
        @Size(max = 50) String clienteTelefono,
        @Size(max = 255) String indirizzo,
        @Size(max = 255) String apparecchio,
        @NotBlank(message = "Il titolo è obbligatorio") @Size(max = 255) String titolo,
        @Size(max = 2000) String note,
        @NotNull(message = "Lo stato è obbligatorio") AppuntamentoAssistenza.Stato stato,
        @NotNull(message = "La data di inizio è obbligatoria") LocalDateTime inizio,
        @NotNull(message = "La data di fine è obbligatoria") LocalDateTime fine,
        @Size(max = 100) String tecnico
) {}
