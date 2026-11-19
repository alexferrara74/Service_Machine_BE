package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pilot.servizi.dto.request.ScaffaleRequest;
import pilot.servizi.dto.response.ScaffaleLayoutResponse;
import pilot.servizi.service.ScaffaleService;

import java.util.List;

@RestController
@RequestMapping("/api/magazzino/scaffali")
@Tag(name = "Scaffali", description = "Scaffali con misure, posizione in pianta ed etichette celle generate automaticamente")
@RequiredArgsConstructor
public class ScaffaleController {

    private final ScaffaleService service;

    @PostMapping
    @Operation(summary = "Crea scaffale e genera le etichette di tutte le celle")
    @ResponseStatus(HttpStatus.CREATED)
    public ScaffaleLayoutResponse create(@Valid @RequestBody ScaffaleRequest dto) {
        return service.create(dto);
    }

    @GetMapping
    @Operation(summary = "Layout completo del magazzino (scaffali, celle e articoli) per la vista 3D")
    public List<ScaffaleLayoutResponse> layout() {
        return service.layout();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio scaffale con celle e articoli")
    public ScaffaleLayoutResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifica misure, posizione o griglia; rigenera le etichette")
    public ScaffaleLayoutResponse update(@PathVariable Long id, @Valid @RequestBody ScaffaleRequest dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina scaffale e celle (solo se vuote)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
