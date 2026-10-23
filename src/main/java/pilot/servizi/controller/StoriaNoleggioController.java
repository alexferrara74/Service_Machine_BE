package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
    @Operation(summary = "Registra nuovo documento di noleggio")
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

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Lista noleggi per cliente")
    public List<StoriaNoleggioResponse> getByCliente(@PathVariable Long clienteId) {
        return service.getByCliente(clienteId);
    }

    @GetMapping("/articolo/{articoloId}")
    @Operation(summary = "Lista noleggi per articolo")
    public List<StoriaNoleggioResponse> getByArticolo(@PathVariable Long articoloId) {
        return service.getByArticolo(articoloId);
    }

    @GetMapping("/matricola/{matricola}")
    @Operation(summary = "Lista noleggi per matricola attrezzatura")
    public List<StoriaNoleggioResponse> getByMatricola(@PathVariable String matricola) {
        return service.getByMatricola(matricola);
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
