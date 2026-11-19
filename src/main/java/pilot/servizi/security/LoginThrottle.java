package pilot.servizi.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/** Blocca per qualche minuto uno username dopo troppi tentativi falliti (in memoria: si azzera al riavvio). */
@Component
public class LoginThrottle {

    public static final int MAX_TENTATIVI = 5;
    static final Duration BLOCCO = Duration.ofMinutes(5);

    private record Stato(int falliti, Instant bloccatoFinoA) {}

    private final ConcurrentHashMap<String, Stato> stati = new ConcurrentHashMap<>();

    public boolean bloccato(String username) {
        Stato s = stati.get(chiave(username));
        return s != null && s.bloccatoFinoA() != null && Instant.now().isBefore(s.bloccatoFinoA());
    }

    public void fallito(String username) {
        stati.merge(chiave(username), new Stato(1, null), (vecchio, nuovo) -> {
            // Un blocco già scaduto riparte da zero.
            int falliti = vecchio.bloccatoFinoA() != null && Instant.now().isAfter(vecchio.bloccatoFinoA()) ? 1 : vecchio.falliti() + 1;
            return new Stato(falliti, falliti >= MAX_TENTATIVI ? Instant.now().plus(BLOCCO) : null);
        });
    }

    public void riuscito(String username) {
        stati.remove(chiave(username));
    }

    private static String chiave(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
