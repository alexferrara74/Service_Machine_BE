package pilot.servizi.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import pilot.servizi.dto.response.LoginResponse;
import pilot.servizi.entity.Utente;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Emette e verifica i token JWT (HS256). Il segreto arriva solo da configurazione: nessun valore predefinito in produzione. */
@Service
public class JwtService {

    private static final int MIN_SECRET_LENGTH = 32;

    private final SecretKey key;
    private final JwtEncoder encoder;
    private final Duration ttl;

    public JwtService(
            @Value("${security.jwt.secret:}") String secret,
            @Value("${security.jwt.ttl-minutes:480}") long ttlMinutes
    ) {
        if (secret == null || secret.trim().length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "Segreto JWT mancante o troppo corto: impostare JWT_SECRET (security.jwt.secret) con almeno "
                            + MIN_SECRET_LENGTH + " caratteri casuali");
        }
        this.key = new SecretKeySpec(secret.trim().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.ttl = Duration.ofMinutes(Math.max(1, ttlMinutes));
    }

    public LoginResponse emetti(Utente utente) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant scadenza = now.plus(ttl);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(utente.getUsername())
                .issuedAt(now)
                .expiresAt(scadenza)
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, scadenza, utente.getUsername(), utente.getRuolo());
    }

    public JwtDecoder decoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt> validator = JwtValidators.createDefault();
        decoder.setJwtValidator(validator);
        return decoder;
    }
}
