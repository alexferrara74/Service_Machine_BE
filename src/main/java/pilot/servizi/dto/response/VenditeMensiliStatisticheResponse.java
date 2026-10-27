package pilot.servizi.dto.response;

public record VenditeMensiliStatisticheResponse(
        Integer anno,
        Integer mese,
        Double quantitaTotale,
        Long righeVendita
) {
}
