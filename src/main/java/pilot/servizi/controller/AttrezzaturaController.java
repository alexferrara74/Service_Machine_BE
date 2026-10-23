package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pilot.servizi.dto.request.AttrezzaturaRequest;
import pilot.servizi.dto.response.AttrezzaturaResponse;
import pilot.servizi.service.AttrezzaturaService;

import java.util.List;

@RestController
@RequestMapping("/api/attrezzature")
@Tag(name = "Attrezzature", description = "Gestione anagrafica attrezzature")
@RequiredArgsConstructor
public class AttrezzaturaController {

    private final AttrezzaturaService service;

    @PostMapping
    @Operation(summary = "Crea nuova attrezzatura")
    @ResponseStatus(HttpStatus.CREATED)
    public AttrezzaturaResponse create(@Valid @RequestBody AttrezzaturaRequest dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio attrezzatura per ID")
    public AttrezzaturaResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/matricola/{matricola}")
    @Operation(summary = "Dettaglio attrezzatura per matricola")
    public AttrezzaturaResponse getByMatricola(@PathVariable String matricola) {
        return service.getByMatricola(matricola);
    }

    @GetMapping
    @Operation(summary = "Lista tutte le attrezzature")
    public List<AttrezzaturaResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/stato/{stato}")
    @Operation(summary = "Filtra attrezzature per stato")
    public List<AttrezzaturaResponse> getByStato(@PathVariable String stato) {
        return service.getByStato(stato);
    }

    @GetMapping("/marca/{marca}")
    @Operation(summary = "Filtra attrezzature per marca")
    public List<AttrezzaturaResponse> getByMarca(@PathVariable String marca) {
        return service.getByMarca(marca);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna attrezzatura")
    public AttrezzaturaResponse update(@PathVariable Long id, @Valid @RequestBody AttrezzaturaRequest dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina attrezzatura")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
