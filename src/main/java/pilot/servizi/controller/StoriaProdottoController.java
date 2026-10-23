package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pilot.servizi.dto.request.StoriaProdottoRequest;
import pilot.servizi.dto.response.StoriaProdottoResponse;
import pilot.servizi.service.StoriaProdottiService;

import java.util.List;

@RestController
@RequestMapping("/api/storia/prodotti")
@Tag(name = "Storia Prodotti", description = "Consultazione e gestione storico prodotti e consumi clienti")
@RequiredArgsConstructor
public class StoriaProdottoController {

    private final StoriaProdottiService service;

    @PostMapping
    @Operation(summary = "Registra voce storico prodotto")
    @ResponseStatus(HttpStatus.CREATED)
    public StoriaProdottoResponse create(@Valid @RequestBody StoriaProdottoRequest dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio voce storico prodotto per ID")
    public StoriaProdottoResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(summary = "Lista tutte le voci dello storico prodotti")
    public List<StoriaProdottoResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Lista storico prodotti per cliente")
    public List<StoriaProdottoResponse> getByCliente(@PathVariable Long clienteId) {
        return service.getByCliente(clienteId);
    }

    @GetMapping("/articolo/{articoloId}")
    @Operation(summary = "Lista storico prodotti per articolo")
    public List<StoriaProdottoResponse> getByArticolo(@PathVariable Long articoloId) {
        return service.getByArticolo(articoloId);
    }

    @GetMapping("/codice/{codiceArticolo}")
    @Operation(summary = "Lista storico prodotti per codice articolo")
    public List<StoriaProdottoResponse> getByCodiceArticolo(@PathVariable String codiceArticolo) {
        return service.getByCodiceArticolo(codiceArticolo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna voce storico prodotto")
    public StoriaProdottoResponse update(@PathVariable Long id, @Valid @RequestBody StoriaProdottoRequest dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina voce storico prodotto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
