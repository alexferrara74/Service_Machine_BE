package pilot.servizi.dto.response;

public record CategoriaVenditeStatisticheResponse(
        Integer categoriaId,
        String categoria,
        Double quantitaTotale,
        Long righeVendita
) {
}
