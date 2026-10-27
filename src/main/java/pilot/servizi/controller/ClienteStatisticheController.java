package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pilot.servizi.dto.response.ClienteStatisticheResponse;
import pilot.servizi.service.ClienteStatisticheService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistiche/clienti")
@Tag(name = "Statistiche clienti", description = "Noleggi e stime di vendita aggregate per cliente")
@RequiredArgsConstructor
public class ClienteStatisticheController {

    private final ClienteStatisticheService service;

    @GetMapping("/{clienteId}")
    @Operation(summary = "Statistiche vendite, categorie e noleggi del cliente")
    public ClienteStatisticheResponse getStatistiche(
            @PathVariable Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate al
    ) {
        return service.getStatistiche(clienteId, dal, al);
    }
}
