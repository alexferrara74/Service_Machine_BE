package pilot.servizi.service;

final class Texts {

    private Texts() {}

    /** Testo ripulito dagli spazi; vuoto o solo spazi diventa null, come un campo non compilato. */
    static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
