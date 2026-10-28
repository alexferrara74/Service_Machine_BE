package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pilot.servizi.dto.response.PageResponse;
import pilot.servizi.dto.request.StoriaNoleggioRequest;
import pilot.servizi.dto.response.StoriaNoleggioResponse;
import pilot.servizi.service.StoriaNoleggiService;

import java.util.List;

@RestController
@RequestMapping("/api/storia/noleggi")
@Tag(name = "Storia Noleggi", description = "Consultazione e gestione storico contratti e documenti di noleggio")
@RequiredArgsConstructor
public class StoriaNoleggioController {

    private final StoriaNoleggiService service;

    @PostMapping
    @Operation(summary = "Registra un nuovo noleggio inserito dall'operatore; docId è generato dal sistema")
    @ResponseStatus(HttpStatus.CREATED)
    public StoriaNoleggioResponse create(@Valid @RequestBody StoriaNoleggioRequest dto) {
        return service.create(dto);
    }

    @GetMapping("/{docId}")
    @Operation(summary = "Dettaglio noleggio per ID documento")
    public StoriaNoleggioResponse getById(@PathVariable Integer docId) {
        return service.getById(docId);
    }

    @GetMapping
    @Operation(summary = "Lista tutti i documenti di noleggio")
    public List<StoriaNoleggioResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/pagina")
    @Operation(summary = "Lista paginata dei documenti di noleggio")
    public PageResponse<StoriaNoleggioResponse> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.getPage(page, size);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Lista noleggi per cliente")
    public List<StoriaNoleggioResponse> getByCliente(@PathVariable Long clienteId) {
        return service.getByCliente(clienteId);
    }

    @GetMapping("/cliente/{clienteId}/pagina")
    @Operation(summary = "Lista paginata dei noleggi per cliente")
    public PageResponse<StoriaNoleggioResponse> getPageByCliente(
            @PathVariable Long clienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.getPageByCliente(clienteId, page, size);
    }

    @GetMapping("/articolo/{articoloId}")
    @Operation(summary = "Lista noleggi per articolo")
    public List<StoriaNoleggioResponse> getByArticolo(@PathVariable Long articoloId) {
        return service.getByArticolo(articoloId);
    }

    @GetMapping("/articolo/{articoloId}/pagina")
    @Operation(summary = "Lista paginata dei noleggi per articolo")
    public PageResponse<StoriaNoleggioResponse> getPageByArticolo(
            @PathVariable Long articoloId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.getPageByArticolo(articoloId, page, size);
    }

    @GetMapping("/matricola/{matricola}")
    @Operation(summary = "Lista noleggi per matricola attrezzatura")
    public List<StoriaNoleggioResponse> getByMatricola(@PathVariable String matricola) {
        return service.getByMatricola(matricola);
    }

    @GetMapping("/matricola/{matricola}/pagina")
    @Operation(summary = "Lista paginata dei noleggi per matricola")
    public PageResponse<StoriaNoleggioResponse> getPageByMatricola(
            @PathVariable String matricola,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.getPageByMatricola(matricola, page, size);
    }

    @PutMapping("/{docId}")
    @Operation(summary = "Aggiorna documento di noleggio")
    public StoriaNoleggioResponse update(@PathVariable Integer docId, @Valid @RequestBody StoriaNoleggioRequest dto) {
        return service.update(docId, dto);
    }

    @DeleteMapping("/{docId}")
    @Operation(summary = "Elimina documento di noleggio")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer docId) {
        service.delete(docId);
    }
}
