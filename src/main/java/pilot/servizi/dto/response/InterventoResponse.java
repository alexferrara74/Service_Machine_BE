package pilot.servizi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InterventoResponse(
        Long interventoId,
        Long clienteId,
        String ragioneSocialeCliente,
        String matricolaAttrezzatura,
        String titolo,
        String descrizioneProblema,
        String soluzioneApplicata,
        String stato,
        LocalDateTime dataApertura,
        LocalDateTime dataChiusura,
        BigDecimal costoManodopera
) {}