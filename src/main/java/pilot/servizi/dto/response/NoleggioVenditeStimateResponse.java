package pilot.servizi.dto.response;

import java.util.List;

public record NoleggioVenditeStimateResponse(
        Integer docId,
        Long articoloIdAttrezzatura,
        String matricola,
        Double quantitaNoleggiata,
        List<CategoriaVenditeStatisticheResponse> categorieProdottoStimate,
        List<ProdottoVenditeStatisticheResponse> prodottiAcquistatiStimati,
        String ambitoStima
) {
}
