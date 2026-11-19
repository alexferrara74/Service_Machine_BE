package pilot.servizi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Articolo creato a mano nel catalogo (es. un'attrezzatura che l'ERP non ha): sempre di origine MANUALE. */
public record ProdottoCatalogoRequest(
        @NotBlank(message = "Il codice articolo è obbligatorio") @Size(max = 50) String codice,
        @NotBlank(message = "La descrizione è obbligatoria") @Size(max = 255) String descrizione,
        /** Se omesso l'articolo è un'attrezzatura (è il caso d'uso del noleggio). */
        Boolean isAttrezzatura
) {}
