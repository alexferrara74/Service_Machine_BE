package pilot.servizi.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AppuntamentoResponse(
        Long id,
        Integer docId,
        Long clienteId,
        String cliente,
        String matricola,
        LocalDateTime dataPrevista,
        String stato,
        String tipo,
        String esito,
        String resoconto,
        Integer giorniProrogaOrdinaria,
        Integer giorniProrogaStraordinaria,
        Integer intervalloManutenzioneGiorni,
        LocalDateTime completatoIl,
        List<Foto> foto
) {
    public record Foto(Long id, String nomeFile) {}
}
