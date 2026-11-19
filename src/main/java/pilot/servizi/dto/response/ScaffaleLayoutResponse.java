package pilot.servizi.dto.response;

import java.util.List;

public record ScaffaleLayoutResponse(
        Long scaffaleId,
        String codice,
        String zona,
        Integer larghezzaCm,
        Integer profonditaCm,
        Integer altezzaCm,
        Integer posXCm,
        Integer posYCm,
        Integer rotazioneGradi,
        Integer numeroRipiani,
        Integer posizioniPerRipiano,
        List<Cella> celle
) {
    public record Cella(
            Long ubicazioneId,
            String etichetta,
            Integer ripiano,
            Integer posizione,
            List<ArticoloInCella> articoli
    ) {}

    public record ArticoloInCella(
            Long posizioneId,
            Long articoloId,
            String codice,
            String descrizione,
            Integer quantita
    ) {}
}
