package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pilot.servizi.dto.request.InterventoRequest;
import pilot.servizi.dto.request.RicambioRequest;
import pilot.servizi.dto.response.InterventoResponse;
import pilot.servizi.dto.response.RicambioResponse;
import pilot.servizi.service.InterventiService;
import pilot.servizi.service.RicambisticaInterventiService;

import java.util.List;

@RestController
@RequestMapping("/api/sync")
@Tag(name = "Sync", description = "API per sincronizzazione con client esterni/offline")
@RequiredArgsConstructor
public class SyncController {

    private final InterventiService interventiService;
    private final RicambisticaInterventiService ricambiService;

    public record SyncInterventoRequest(
            @Valid @NotNull InterventoRequest intervento,
            @Valid List<RicambioRequest> ricambi
    ) {}

    public record SyncInterventoResponse(
            InterventoResponse intervento,
            List<RicambioResponse> ricambi
    ) {}

    @PostMapping("/intervento")
    @Transactional
    @Operation(summary = "Sincronizza intervento completo (testata + ricambi)")
    public SyncInterventoResponse syncIntervento(@Valid @RequestBody SyncInterventoRequest dto) {
        InterventoResponse saved = interventiService.create(dto.intervento());
        List<RicambioResponse> ricambiSalvati = dto.ricambi() == null ? List.of() :
                dto.ricambi().stream()
                        .map(r -> new RicambioRequest(saved.interventoId(), r.articoloId(),
                                r.codiceRicambio(), r.descrizioneRicambio(),
                                r.quantitaUtilizzata(), r.prezzoUnitario()))
                        .map(ricambiService::create)
                        .toList();
        return new SyncInterventoResponse(saved, ricambiSalvati);
    }
}