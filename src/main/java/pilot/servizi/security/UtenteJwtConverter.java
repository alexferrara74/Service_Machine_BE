package pilot.servizi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import pilot.servizi.entity.Utente;
import pilot.servizi.repository.UtenteRepository;

import java.time.ZoneId;
import java.util.List;

/**
 * Il ruolo non si legge dal token ma dal database a ogni richiesta: un utente disattivato, eliminato o
 * declassato perde subito i permessi, e un token emesso prima di un cambio password non vale più.
 */
@Component
@RequiredArgsConstructor
public class UtenteJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UtenteRepository repo;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Utente utente = repo.findByUsernameIgnoreCase(jwt.getSubject())
                .filter(Utente::isAttivo)
                .orElseThrow(() -> rifiutato("Utente non valido o disattivato"));
        if (jwt.getIssuedAt() != null
                && jwt.getIssuedAt().isBefore(utente.getPasswordModificataIl().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS))) {
            throw rifiutato("Token emesso prima dell'ultimo cambio password");
        }
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name())), utente.getUsername());
    }

    private static OAuth2AuthenticationException rifiutato(String descrizione) {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_token", descrizione, null));
    }
}
