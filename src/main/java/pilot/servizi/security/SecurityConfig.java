package pilot.servizi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import pilot.servizi.service.RegistroService;
import pilot.servizi.dto.response.ErrorResponse;
import pilot.servizi.entity.Utente.Ruolo;
import pilot.servizi.log.ErrorConstants;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Regole di accesso:
 * <ul>
 *   <li>login pubblico;</li>
 *   <li>/api/utenti e /api/registro solo ADMIN;</li>
 *   <li>cambio password e profilo per qualsiasi utente autenticato;</li>
 *   <li>letture (GET/HEAD/OPTIONS) per ADMIN e VIEWER; ogni altro metodo solo ADMIN.</li>
 * </ul>
 * Il controllo è sempre lato server: nascondere i pulsanti nel frontend è solo comodità.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, UtenteJwtConverter converter, ObjectMapper mapper, RegistroService registro) throws Exception {
        AuthenticationEntryPoint entryPoint = (req, res, ex) ->
                scrivi(mapper, req, res, 401, "UNAUTHORIZED", ErrorConstants.AUTH_NON_AUTENTICATO);
        AccessDeniedHandler denied = (req, res, ex) ->
                scrivi(mapper, req, res, 403, "FORBIDDEN", ErrorConstants.AUTH_NON_AUTORIZZATO);

        http
                .csrf(AbstractHttpConfigurer::disable) // API senza cookie di sessione: autenticazione solo con Bearer token
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        .requestMatchers("/api/utenti/**", "/api/registro/**").hasRole(Ruolo.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole(Ruolo.ADMIN.name(), Ruolo.VIEWER.name())
                        .requestMatchers(HttpMethod.HEAD, "/api/**").hasAnyRole(Ruolo.ADMIN.name(), Ruolo.VIEWER.name())
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers("/api/**").hasRole(Ruolo.ADMIN.name())
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(denied))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(denied))
                .addFilterAfter(new RegistroFilter(registro), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(JwtService jwtService) {
        return jwtService.decoder();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void scrivi(ObjectMapper mapper, HttpServletRequest req, HttpServletResponse res,
                               int status, String error, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(), new ErrorResponse(LocalDateTime.now(), status, error, message, req.getRequestURI()));
    }
}
