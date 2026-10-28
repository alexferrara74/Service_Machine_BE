package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pilot.servizi.dto.response.ProdottoCatalogoResponse;
import pilot.servizi.service.ProdottiCatalogoService;

import java.util.List;

@RestController
@RequestMapping("/api/prodotti/catalogo")
@Tag(name = "Catalogo Prodotti", description = "Catalogo ERP sincronizzato di prodotti e attrezzature")
@RequiredArgsConstructor
public class ProdottiCatalogoController {

    private final ProdottiCatalogoService service;

    @GetMapping
    @Operation(summary = "Elenco prodotti di catalogo con filtri opzionali")
    public List<ProdottoCatalogoResponse> getAll(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Boolean attrezzatura,
            @RequestParam(required = false) Boolean accessorio
    ) {
        return service.getAll(categoriaId, attrezzatura, accessorio);
    }

    @GetMapping("/{articoloId}")
    @Operation(summary = "Dettaglio prodotto di catalogo")
    public ProdottoCatalogoResponse getById(@PathVariable Long articoloId) {
        return service.getById(articoloId);
    }
}
