package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pilot.servizi.dto.response.PageResponse;
import pilot.servizi.dto.response.RegistroAttivitaResponse;
import pilot.servizi.service.RegistroService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/registro")
@Tag(name = "Registro", description = "Registro di accessi e operazioni degli utenti (solo amministratori, sola lettura)")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService service;

    @GetMapping
    @Operation(summary = "Cerca nel registro, dal più recente; filtri tutti facoltativi; 'a' è escluso")
    public PageResponse<RegistroAttivitaResponse> cerca(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String azione,
            @RequestParam(required = false) String esito,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime da,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime a,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.cerca(username, azione, esito, da, a, page, size);
    }
}
