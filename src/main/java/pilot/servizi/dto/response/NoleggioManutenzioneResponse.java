package pilot.servizi.dto.response;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public record NoleggioManutenzioneResponse(
        Integer docId,
        Long clienteId,
        String cliente,
        Long articoloId,
        String matricola,
        Double quantitaNoleggiata,
        boolean configurata,
        Double sogliaQuantita,
        Integer ordinariePerStraordinaria,
        List<Integer> categoriaIds,
        Double quantitaAcquistataTotale,
        Double quantitaDopoUltimaManutenzione,
        Double quantitaResidua,
        int manutenzioniOrdinarieDallUltimaStraordinaria,
        boolean manutenzioneDovuta,
        String tipoManutenzioneDovuta,
        LocalDateTime ultimaManutenzioneIl,
        boolean pianificazioneConfigurata,
        LocalDate prossimaManutenzioneIl,
        Integer giorniAllaManutenzione,
        String statoManutenzione,
        Integer intervalloManutenzioneGiorni
) {
}
