package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pilot.servizi.dto.request.NoleggioManutenzionePianificazioneRequest;
import pilot.servizi.dto.response.ClienteManutenzionePianificazioneResponse;
import pilot.servizi.service.NoleggioManutenzioneService;

@RestController
@RequestMapping("/api/clienti/{clienteId}/manutenzione/pianificazione")
@Tag(name = "Manutenzione clienti", description = "Configurazione manutentiva condivisa tra i noleggi del cliente")
@RequiredArgsConstructor
public class ClienteManutenzioneController {

    private final NoleggioManutenzioneService service;

    @GetMapping
    @Operation(summary = "Legge la pianificazione stagionale condivisa del cliente")
    public ClienteManutenzionePianificazioneResponse getPianificazione(@PathVariable Long clienteId) {
        return service.getPianificazioneCliente(clienteId);
    }

    @PutMapping
    @Operation(summary = "Configura gli intervalli stagionali condivisi per i noleggi del cliente")
    public ClienteManutenzionePianificazioneResponse configuraPianificazione(
            @PathVariable Long clienteId,
            @Valid @RequestBody NoleggioManutenzionePianificazioneRequest request
    ) {
        return service.configuraPianificazioneCliente(clienteId, request);
    }
}
