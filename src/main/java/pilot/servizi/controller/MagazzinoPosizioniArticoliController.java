package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pilot.servizi.dto.request.PosizioneArticoloRequest;
import pilot.servizi.dto.response.PosizioneArticoloResponse;
import pilot.servizi.service.MagazzinoPosizioniArticoliService;

import java.util.List;

@RestController
@RequestMapping("/api/magazzino/posizioni")
@Tag(name = "Magazzino Posizioni", description = "Gestione giacenze e posizioni articoli a magazzino")
@RequiredArgsConstructor
public class MagazzinoPosizioniArticoliController {

    private final MagazzinoPosizioniArticoliService service;

    @PostMapping
    @Operation(summary = "Registra posizione articolo in magazzino")
    @ResponseStatus(HttpStatus.CREATED)
    public PosizioneArticoloResponse create(@Valid @RequestBody PosizioneArticoloRequest dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio posizione articolo per ID")
    public PosizioneArticoloResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(summary = "Lista tutte le posizioni articolo")
    public List<PosizioneArticoloResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/ubicazione/{ubicazioneId}")
    @Operation(summary = "Lista articoli in una determinata ubicazione")
    public List<PosizioneArticoloResponse> getByUbicazione(@PathVariable Long ubicazioneId) {
        return service.getByUbicazione(ubicazioneId);
    }

    @GetMapping("/articolo/{articoloId}")
    @Operation(summary = "Lista posizioni occupate da un determinato articolo")
    public List<PosizioneArticoloResponse> getByArticolo(@PathVariable Long articoloId) {
        return service.getByArticolo(articoloId);
    }

    @GetMapping("/articolo/{articoloId}/giacenza")
    @Operation(summary = "Calcola giacenza totale articolo su tutte le ubicazioni")
    public Integer getGiacenzaTotale(@PathVariable Long articoloId) {
        return service.giacenzaTotaleArticolo(articoloId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna posizione articolo")
    public PosizioneArticoloResponse update(@PathVariable Long id, @Valid @RequestBody PosizioneArticoloRequest dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina posizione articolo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
