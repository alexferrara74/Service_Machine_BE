package pilot.servizi.dto.response;

public record RicambioResponse(
        Long id,
        Long interventoId,
        Long articoloId,
        String codiceRicambio,
        String descrizioneRicambio,
        Integer quantitaUtilizzata,
        Double prezzoUnitario
) {}