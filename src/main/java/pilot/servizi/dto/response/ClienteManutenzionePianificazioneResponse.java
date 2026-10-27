package pilot.servizi.dto.response;

import java.util.List;

public record ClienteManutenzionePianificazioneResponse(
        Long clienteId,
        Integer giorniAvviso,
        List<NoleggioManutenzionePeriodoResponse> periodi
) {
}
