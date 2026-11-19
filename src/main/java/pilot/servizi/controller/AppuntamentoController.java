package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pilot.servizi.dto.request.AppuntamentoRequest;
import pilot.servizi.dto.request.AppuntamentoStatoRequest;
import pilot.servizi.dto.response.AppuntamentoResponse;
import pilot.servizi.service.AppuntamentoAssistenzaService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appuntamenti")
@Tag(name = "Appuntamenti", description = "Calendario delle assistenze su noleggi censiti o clienti non censiti")
@RequiredArgsConstructor
public class AppuntamentoController {

    private final AppuntamentoAssistenzaService service;

    @GetMapping
    @Operation(summary = "Appuntamenti nell'intervallo [da, a) per il calendario")
    public List<AppuntamentoResponse> getByIntervallo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime da,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime a
    ) {
        return service.getByIntervallo(da, a);
    }

    @GetMapping("/noleggio/{docId}")
    @Operation(summary = "Appuntamenti di un noleggio censito")
    public List<AppuntamentoResponse> getByNoleggio(@PathVariable Integer docId) {
        return service.getByNoleggio(docId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio appuntamento")
    public AppuntamentoResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Crea un appuntamento")
    @ResponseStatus(HttpStatus.CREATED)
    public AppuntamentoResponse create(@Valid @RequestBody AppuntamentoRequest dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna un appuntamento")
    public AppuntamentoResponse update(@PathVariable Long id, @Valid @RequestBody AppuntamentoRequest dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}/stato")
    @Operation(summary = "Cambia lo stato (da confermare, da fare, fatto, annullato)")
    public AppuntamentoResponse updateStato(@PathVariable Long id, @Valid @RequestBody AppuntamentoStatoRequest dto) {
        return service.updateStato(id, dto.stato());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un appuntamento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
