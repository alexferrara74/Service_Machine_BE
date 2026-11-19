package pilot.servizi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pilot.servizi.entity.Utente;
import pilot.servizi.repository.UtenteRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Se non esiste alcun utente crea il primo amministratore, altrimenti nessuno potrebbe accedere.
 * Credenziali da ADMIN_USERNAME / ADMIN_PASSWORD; se la password manca ne genera una casuale e la scrive
 * una sola volta nel log (da cambiare subito dopo il primo accesso).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final UtenteRepository repo;
    private final PasswordEncoder encoder;

    @Value("${security.bootstrap.admin-username:admin}")
    private String username;

    @Value("${security.bootstrap.admin-password:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repo.count() > 0) {
            return;
        }
        String nome = username == null || username.isBlank() ? "admin" : username.trim();
        boolean generata = password == null || password.isBlank();
        String pwd = generata ? casuale(20) : password;
        if (!PasswordPolicy.valida(pwd, nome)) {
            throw new IllegalStateException("ADMIN_PASSWORD non valida: servono almeno 10 caratteri (massimo 72), diversi dallo username");
        }
        LocalDateTime now = LocalDateTime.now();
        repo.save(Utente.builder()
                .username(nome)
                .passwordHash(encoder.encode(pwd))
                .ruolo(Utente.Ruolo.ADMIN)
                .attivo(true)
                .passwordModificataIl(now)
                .creatoIl(now)
                .build());
        if (generata) {
            log.warn("Creato il primo amministratore '{}' con password generata: {}  (annotala e cambiala al primo accesso; non verrà più mostrata)", nome, pwd);
        } else {
            log.info("Creato il primo amministratore '{}' dalle variabili d'ambiente", nome);
        }
    }

    private static String casuale(int lunghezza) {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(lunghezza);
        for (int i = 0; i < lunghezza; i++) sb.append(ALFABETO.charAt(r.nextInt(ALFABETO.length())));
        return sb.toString();
    }
}
