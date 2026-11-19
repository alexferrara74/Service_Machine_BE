package pilot.servizi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pilot.servizi.service.RegistroService;

import java.io.IOException;

/**
 * Registra le richieste di scrittura (e le esportazioni) degli utenti autenticati, comprese quelle respinte con 403:
 * stanno dopo l'autenticazione ma prima della verifica dei permessi, così vede anche i tentativi non consentiti.
 * Non è un bean: Spring Boot registrerebbe ogni Filter bean anche fuori dalla catena di sicurezza.
 */
@RequiredArgsConstructor
public class RegistroFilter extends OncePerRequestFilter {

    private final RegistroService registro;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Il login ha un proprio evento; le sole letture non si registrano.
        String metodo = request.getMethod();
        boolean soloLettura = "GET".equalsIgnoreCase(metodo) || "HEAD".equalsIgnoreCase(metodo) || "OPTIONS".equalsIgnoreCase(metodo);
        return !path.startsWith("/api/") || path.equals("/api/auth/login") || (soloLettura && !path.contains("/export"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                String ruolo = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .map(a -> a.replace("ROLE_", "")).findFirst().orElse(null);
                registro.registraRichiesta(auth.getName(), ruolo, request.getMethod(), request.getRequestURI(),
                        response.getStatus(), request.getRemoteAddr());
            }
        }
    }
}
