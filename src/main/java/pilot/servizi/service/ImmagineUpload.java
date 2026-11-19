package pilot.servizi.service;

/** Controlli comuni alle foto allegate agli appuntamenti. */
final class ImmagineUpload {

    static final long MAX_BYTES = 10L * 1024 * 1024;
    static final int MAX_FOTO = 20;

    private ImmagineUpload() {}

    /** Solo JPEG, PNG e WebP, riconosciuti dai primi byte e non dal nome del file. Null se non è un'immagine ammessa. */
    static String riconosci(byte[] c) {
        if (c.length > 3 && (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (c.length > 8 && (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G') {
            return "image/png";
        }
        if (c.length > 12 && c[0] == 'R' && c[1] == 'I' && c[2] == 'F' && c[3] == 'F'
                && c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    /** Nome file senza percorso né caratteri strani, con l'estensione del tipo riconosciuto. */
    static String nomeSicuro(String originale, String tipo) {
        String estensione = switch (tipo) { case "image/png" -> ".png"; case "image/webp" -> ".webp"; default -> ".jpg"; };
        String base = originale == null ? "foto" : originale.replaceAll("^.*[\\\\/]", "").replaceAll("\\.[^.]*$", "");
        base = base.replaceAll("[^A-Za-z0-9_-]", "_");
        if (base.isBlank()) base = "foto";
        if (base.length() > 80) base = base.substring(0, 80);
        return base + estensione;
    }
}
