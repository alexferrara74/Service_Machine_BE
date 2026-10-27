package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pilot.servizi.dto.request.NoleggioManutenzioneConfigurazioneRequest;
import pilot.servizi.dto.request.NoleggioManutenzioneRequest;
import pilot.servizi.dto.response.NoleggioManutenzioneEventoResponse;
import pilot.servizi.dto.response.NoleggioManutenzioneResponse;
import pilot.servizi.service.NoleggioManutenzioneService;

import java.util.List;

@RestController
@RequestMapping("/api/storia/noleggi/{docId}/manutenzione")
@Tag(name = "Manutenzione noleggi", description = "Pianificazione stagionale e storico manutenzioni per apparecchio a noleggio")
@RequiredArgsConstructor
public class NoleggioManutenzioneController {

    private final NoleggioManutenzioneService service;

    @GetMapping
    @Operation(summary = "Stato manutenzione, consumi e soglia per un noleggio")
    public NoleggioManutenzioneResponse getStato(@PathVariable Integer docId) {
        return service.getStato(docId);
    }

    @PutMapping("/configurazione")
    @Operation(summary = "Configura le categorie prodotto usate come stima aggregata per il cliente")
    public NoleggioManutenzioneResponse configura(
            @PathVariable Integer docId,
            @Valid @RequestBody NoleggioManutenzioneConfigurazioneRequest request
    ) {
        return service.configura(docId, request);
    }

    @GetMapping("/eventi")
    @Operation(summary = "Elenco manutenzioni effettuate per il noleggio")
    public List<NoleggioManutenzioneEventoResponse> getEventi(@PathVariable Integer docId) {
        return service.getEventi(docId);
    }

    @PostMapping("/eventi")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra la manutenzione dovuta secondo la soglia raggiunta")
    public NoleggioManutenzioneEventoResponse registraManutenzione(
            @PathVariable Integer docId,
            @Valid @RequestBody NoleggioManutenzioneRequest request
    ) {
        return service.registraManutenzione(docId, request);
    }

    @PostMapping("/eventi/straordinaria")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una manutenzione straordinaria per guasto o segnalazione")
    public NoleggioManutenzioneEventoResponse registraStraordinaria(
            @PathVariable Integer docId,
            @Valid @RequestBody NoleggioManutenzioneRequest request
    ) {
        return service.registraStraordinaria(docId, request);
    }
}
