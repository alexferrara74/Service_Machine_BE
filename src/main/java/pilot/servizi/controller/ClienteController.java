package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pilot.servizi.dto.request.ClienteRequest;
import pilot.servizi.dto.response.ClienteResponse;
import pilot.servizi.service.ClientiService;
import java.util.List;


@RestController
@RequestMapping("/api/clienti")
@Tag(name = "Clienti", description = "Anagrafica clienti")
@RequiredArgsConstructor
public class ClienteController {

    private final ClientiService service;

    @PostMapping
    @Operation(summary = "Crea un nuovo cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creato"),
            @ApiResponse(responseCode = "409", description = "Codice già esistente")
    })
    public ResponseEntity<ClienteResponse> create(@Valid @RequestBody ClienteRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio cliente per ID")
    public ResponseEntity<ClienteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lista tutti i clienti")
    public ResponseEntity<List<ClienteResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna cliente esistente")
    public ResponseEntity<ClienteResponse> update(@PathVariable Long id, @Valid @RequestBody ClienteRequest dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina cliente")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}