package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pilot.servizi.dto.request.UtenteAggiornamentoRequest;
import pilot.servizi.dto.request.UtenteRequest;
import pilot.servizi.dto.response.UtenteResponse;
import pilot.servizi.service.UtenteService;

import java.util.List;

@RestController
@RequestMapping("/api/utenti")
@Tag(name = "Utenti", description = "Gestione utenti e ruoli (solo amministratori)")
@RequiredArgsConstructor
public class UtenteController {

    private final UtenteService service;

    @GetMapping
    @Operation(summary = "Elenco utenti")
    public List<UtenteResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    @Operation(summary = "Crea un utente (ADMIN o VIEWER)")
    @ResponseStatus(HttpStatus.CREATED)
    public UtenteResponse create(@Valid @RequestBody UtenteRequest dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cambia ruolo, attiva/disattiva, resetta la password")
    public UtenteResponse update(@PathVariable Long id, @Valid @RequestBody UtenteAggiornamentoRequest dto, Authentication auth) {
        return service.update(id, dto, auth.getName());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un utente")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, auth.getName());
    }
}
