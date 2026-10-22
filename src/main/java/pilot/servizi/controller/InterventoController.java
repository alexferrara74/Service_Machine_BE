package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pilot.servizi.dto.request.InterventoRequest;
import pilot.servizi.dto.request.RicambioRequest;
import pilot.servizi.dto.response.InterventoResponse;
import pilot.servizi.dto.response.RicambioResponse;
import pilot.servizi.service.InterventiService;
import pilot.servizi.service.RicambisticaInterventiService;
import java.util.List;


@RestController
@RequestMapping("/api/interventi")
@Tag(name = "Interventi", description = "Gestione interventi tecnici")
@RequiredArgsConstructor
public class InterventoController {

    private final InterventiService service;
    private final RicambisticaInterventiService ricambiService;

    @PostMapping
    @Operation(summary = "Apri un nuovo intervento")
    @ResponseStatus(HttpStatus.CREATED)
    public InterventoResponse create(@Valid @RequestBody InterventoRequest dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio intervento")
    public InterventoResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(summary = "Lista interventi (tutti)")
    public List<InterventoResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Lista interventi per cliente")
    public List<InterventoResponse> getByCliente(@PathVariable Long clienteId) {
        return service.getByCliente(clienteId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna intervento")
    public InterventoResponse update(@PathVariable Long id, @Valid @RequestBody InterventoRequest dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // --- Sub-resource: ricambi dell'intervento ---
    @GetMapping("/{id}/ricambi")
    @Operation(summary = "Lista ricambi usati nell'intervento")
    public List<RicambioResponse> getRicambi(@PathVariable Long id) {
        return ricambiService.getByIntervento(id);
    }

    @PostMapping("/{id}/ricambi")
    @Operation(summary = "Aggiungi ricambio all'intervento")
    @ResponseStatus(HttpStatus.CREATED)
    public RicambioResponse addRicambio(@PathVariable Long id, @Valid @RequestBody RicambioRequest dto) {
        // Forziamo l'interventoId dal path
        RicambioRequest fixed = new RicambioRequest(id, dto.articoloId(), dto.codiceRicambio(),
                dto.descrizioneRicambio(), dto.quantitaUtilizzata(), dto.prezzoUnitario());
        return ricambiService.create(fixed);
    }

    @DeleteMapping("/{id}/ricambi/{ricambioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRicambio(@PathVariable Long id, @PathVariable Long ricambioId) {
        ricambiService.delete(ricambioId);
    }
}