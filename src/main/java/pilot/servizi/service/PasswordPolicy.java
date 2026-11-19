package pilot.servizi.service;

import java.nio.charset.StandardCharsets;

final class PasswordPolicy {

    static final int MIN_LENGTH = 10;
    /** BCrypt considera solo i primi 72 byte: oltre, password diverse sembrerebbero uguali. */
    static final int MAX_BYTES = 72;

    private PasswordPolicy() {}

    static boolean valida(String password, String username) {
        return password != null
                && password.length() >= MIN_LENGTH
                && password.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES
                && !password.equalsIgnoreCase(username);
    }
}
