package pilot.servizi.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ClienteStatisticheResponse(
        Long clienteId,
        String cliente,
        LocalDate dal,
        LocalDate al,
        Double quantitaTotaleAcquistata,
        Long righeVendita,
        List<NoleggioVenditeStimateResponse> noleggiEStimePerAttrezzatura,
        List<CategoriaVenditeStatisticheResponse> venditePerCategoria,
        List<ProdottoVenditeStatisticheResponse> venditePerProdotto,
        List<VenditeMensiliStatisticheResponse> andamentoMensile
) {
}
