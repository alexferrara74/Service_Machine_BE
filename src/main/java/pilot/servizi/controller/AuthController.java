package pilot.servizi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pilot.servizi.dto.request.CambioPasswordRequest;
import pilot.servizi.dto.request.LoginRequest;
import pilot.servizi.dto.response.LoginResponse;
import pilot.servizi.dto.response.UtenteResponse;
import pilot.servizi.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticazione", description = "Login, profilo e cambio password")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    @Operation(summary = "Login: restituisce il token da inviare come 'Authorization: Bearer'")
    public LoginResponse login(@Valid @RequestBody LoginRequest dto) {
        return service.login(dto);
    }

    @GetMapping("/me")
    @Operation(summary = "Utente autenticato e suo ruolo")
    public UtenteResponse me(Authentication auth) {
        return service.me(auth.getName());
    }

    @PostMapping("/password")
    @Operation(summary = "Cambia la propria password (invalida i token emessi prima)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiaPassword(Authentication auth, @Valid @RequestBody CambioPasswordRequest dto) {
        service.cambiaPassword(auth.getName(), dto);
    }
}
