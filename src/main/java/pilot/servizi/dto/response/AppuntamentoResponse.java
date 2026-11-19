package pilot.servizi.dto.response;

import pilot.servizi.entity.AppuntamentoAssistenza;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@code censito} è true quando l'appuntamento è collegato a un noleggio (docId): in quel caso
 * cliente e matricola arrivano dal noleggio, altrimenti dai campi inseriti a mano.
 */
public record AppuntamentoResponse(
        Long id,
        Integer docId,
        boolean censito,
        Long clienteId,
        String cliente,
        String clienteTelefono,
        String indirizzo,
        String matricola,
        String apparecchio,
        String titolo,
        String note,
        AppuntamentoAssistenza.Stato stato,
        LocalDateTime inizio,
        LocalDateTime fine,
        String tecnico,
        List<Foto> foto
) {
    /** Foto allegata: il contenuto si scarica da /api/appuntamenti/{id}/foto/{fotoId}. */
    public record Foto(Long id, String nomeFile) {}
}
