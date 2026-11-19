package pilot.servizi.service;

/**
 * Origine delle anagrafiche condivise con l'ERP e identificativi delle righe create a mano.
 * Le righe ERP arrivano con il loro id; quelle manuali partono da {@link #ID_MANUALE_MINIMO} così non
 * possono collidere con gli id che l'ERP assegnerà in futuro (presupposto: gli id ERP restano sotto il miliardo).
 */
final class Origini {

    static final String ERP = "ERP";
    static final String MANUALE = "MANUALE";
    static final long ID_MANUALE_MINIMO = 1_000_000_000L;

    private Origini() {}

    /** Prossimo id per una riga manuale, dato il massimo attuale della tabella. */
    static long prossimoIdManuale(Long massimoAttuale) {
        return Math.max(ID_MANUALE_MINIMO, (massimoAttuale == null ? 0 : massimoAttuale) + 1);
    }
}
