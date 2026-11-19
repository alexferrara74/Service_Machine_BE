package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotNull;
import pilot.servizi.entity.AppuntamentoAssistenza;

public record AppuntamentoStatoRequest(
        @NotNull(message = "Lo stato è obbligatorio") AppuntamentoAssistenza.Stato stato
) {}
