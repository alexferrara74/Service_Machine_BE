package pilot.servizi.dto.response;

public record ProdottoVenditeStatisticheResponse(
        Long articoloId,
        String codiceArticolo,
        String descrizione,
        Integer categoriaId,
        String categoria,
        Double quantitaTotale,
        Long righeVendita
) {
}
