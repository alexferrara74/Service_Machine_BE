package pilot.servizi.log;

public final class ErrorConstants {

    private ErrorConstants() {}

    // ============================================================
    // STATI DI ERRORE (ERROR STATUS / CODES)
    // ============================================================
    public static final String ERR_VALIDATION = "VALIDATION_ERROR";
    public static final String ERR_NOT_FOUND = "NOT_FOUND";
    public static final String ERR_CONFLICT = "CONFLICT";
    public static final String ERR_INTEGRITY = "DATA_INTEGRITY_VIOLATION";
    public static final String ERR_INTERNAL = "INTERNAL_SERVER_ERROR";

    // ============================================================
    // LOG FORMAT TEMPLATES
    // ============================================================
    public static final String LOG_VALIDATION = "[STATUS: {}] [OP: {}] Errore di validazione dati: {}";
    public static final String LOG_NOT_FOUND = "[STATUS: {}] [OP: {}] Risorsa non trovata: {}";
    public static final String LOG_CONFLICT = "[STATUS: {}] [OP: {}] Conflitto di unicità o integrità dati: {}";
    public static final String LOG_UNEXPECTED = "[STATUS: {}] [OP: {}] Errore imprevisto di sistema: {}";

    // ============================================================
    // MESSAGGI COSTANTI PER ENTITÀ / OPERAZIONI
    // ============================================================

    // --- CLIENTI ---
    public static final String CLIENTE_DTO_NULL = "I dati del cliente non possono essere null";
    public static final String CLIENTE_CODICE_OBBLIGATORIO = "Il codice cliente è obbligatorio e non può essere vuoto";
    public static final String CLIENTE_CODICE_TOO_LONG = "Il codice cliente supera la lunghezza massima di 50 caratteri";
    public static final String CLIENTE_RAGIONE_SOCIALE_OBBLIGATORIA = "La ragione sociale è obbligatoria e non può essere vuota";
    public static final String CLIENTE_RAGIONE_SOCIALE_TOO_LONG = "La ragione sociale supera la lunghezza massima di 255 caratteri";
    public static final String CLIENTE_PIVA_TOO_LONG = "La partita IVA supera la lunghezza massima di 20 caratteri";
    public static final String CLIENTE_CF_TOO_LONG = "Il codice fiscale supera la lunghezza massima di 20 caratteri";
    public static final String CLIENTE_EMAIL_INVALID = "Il formato dell'email del cliente non è valido";
    public static final String CLIENTE_CODICE_DUPLICATO = "Codice cliente già esistente: ";
    public static final String CLIENTE_NON_TROVATO = "Cliente non trovato con ID ";
    public static final String CLIENTE_ELIMINAZIONE_BLOCCATA = "Il cliente non può essere eliminato perché ha noleggi, interventi o storico acquisti associati: ";

    // --- ATTREZZATURE ---
    public static final String ATTREZZATURA_DTO_NULL = "I dati dell'attrezzatura non possono essere null";
    public static final String ATTREZZATURA_MATRICOLA_OBBLIGATORIA = "La matricola dell'attrezzatura è obbligatoria e non può essere vuota";
    public static final String ATTREZZATURA_MATRICOLA_TOO_LONG = "La matricola supera la lunghezza massima di 100 caratteri";
    public static final String ATTREZZATURA_DESCRIZIONE_TOO_LONG = "La descrizione dell'attrezzatura supera i 255 caratteri";
    public static final String ATTREZZATURA_MATRICOLA_DUPLICATA = "Attrezzatura già esistente con matricola: ";
    public static final String ATTREZZATURA_NON_TROVATA_ID = "Attrezzatura non trovata con ID ";
    public static final String ATTREZZATURA_ELIMINAZIONE_BLOCCATA = "L'attrezzatura non può essere eliminata perché è usata da noleggi o interventi: ";
    public static final String ATTREZZATURA_NON_TROVATA_MATRICOLA = "Attrezzatura non trovata con matricola: ";
    public static final String ATTREZZATURA_MATRICOLA_BLOCCATA = "La matricola non può essere modificata perché è usata da noleggi o interventi: ";
    public static final String ATTREZZATURA_STATO_INVALIDO = "Lo stato dell'attrezzatura deve essere DISPONIBILE, IN_USO o IN_MANUTENZIONE";

    // --- INTERVENTI ---
    public static final String INTERVENTO_DTO_NULL = "I dati dell'intervento non possono essere null";
    public static final String INTERVENTO_CLIENTE_OBBLIGATORIO = "L'ID cliente è obbligatorio";
    public static final String INTERVENTO_MATRICOLA_OBBLIGATORIA = "La matricola attrezzatura è obbligatoria e non può essere vuota";
    public static final String INTERVENTO_TITOLO_OBBLIGATORIO = "Il titolo dell'intervento è obbligatorio e non può essere vuoto";
    public static final String INTERVENTO_STATO_OBBLIGATORIO = "Lo stato dell'intervento è obbligatorio e non può essere vuoto";
    public static final String INTERVENTO_DATA_APERTURA_OBBLIGATORIA = "La data di apertura dell'intervento è obbligatoria";
    public static final String INTERVENTO_DATE_INCOERENTI = "La data di chiusura non può precedere la data di apertura";
    public static final String INTERVENTO_COSTO_NEGATIVO = "Il costo della manodopera non può essere negativo";
    public static final String INTERVENTO_NON_TROVATO = "Intervento non trovato con ID ";

    // --- MAGAZZINO DIGITALE ---
    public static final String MAGAZZINO_DTO_NULL = "I dati dell'ubicazione magazzino non possono essere null";
    public static final String MAGAZZINO_CODICE_OBBLIGATORIO = "Il codice ubicazione è obbligatorio e non può essere vuoto";
    public static final String MAGAZZINO_CODICE_TOO_LONG = "Il codice ubicazione supera i 50 caratteri";
    public static final String MAGAZZINO_ZONA_OBBLIGATORIA = "La zona dell'ubicazione è obbligatoria e non può essere vuota";
    public static final String MAGAZZINO_CAPACITA_INVALIDA = "La capacità massima dell'ubicazione deve essere almeno 1";
    public static final String MAGAZZINO_CODICE_DUPLICATO = "Codice ubicazione già utilizzato: ";
    public static final String MAGAZZINO_NON_TROVATO = "Ubicazione magazzino non trovata con ID ";
    public static final String MAGAZZINO_ELIMINAZIONE_BLOCCATA = "L'ubicazione non può essere eliminata perché contiene giacenze di articoli: ";
    public static final String ARTICOLO_NON_TROVATO = "Articolo non trovato nel catalogo con ID ";

    // --- APPUNTAMENTI ---
    public static final String APPUNTAMENTO_NON_TROVATO = "Appuntamento non trovato con ID ";
    public static final String APPUNTAMENTO_TIPO_INVALIDO = "Il tipo deve essere ORDINARIA o STRAORDINARIA";
    public static final String APPUNTAMENTO_ESITO_INVALIDO = "L'esito deve essere POSITIVO o NEGATIVO";
    public static final String APPUNTAMENTO_ESITO_OBBLIGATORIO = "Per chiudere l'appuntamento indica l'esito (positivo o negativo)";
    public static final String APPUNTAMENTO_RESOCONTO_OBBLIGATORIO = "Per chiudere l'appuntamento scrivi il resoconto";
    public static final String APPUNTAMENTO_NON_MODIFICABILE = "L'appuntamento è già chiuso o annullato e non può essere modificato: ";
    public static final String APPUNTAMENTO_ELIMINAZIONE_BLOCCATA = "Un appuntamento completato non può essere eliminato: ";
    public static final String FOTO_NON_TROVATA = "Foto non trovata con ID ";
    public static final String FOTO_NON_VALIDA = "La foto deve essere un'immagine JPEG, PNG o WebP non vuota";
    public static final String FOTO_TROPPO_GRANDE = "La foto supera la dimensione massima di 10 MB";
    public static final String FOTO_LIMITE = "Raggiunto il numero massimo di 20 foto per appuntamento";

    // --- SCAFFALI 3D ---
    public static final String SCAFFALE_DTO_NULL = "I dati dello scaffale non possono essere null";
    public static final String SCAFFALE_NON_TROVATO = "Scaffale non trovato con ID ";
    public static final String SCAFFALE_CODICE_DUPLICATO = "Codice scaffale già utilizzato: ";
    public static final String SCAFFALE_CODICE_NON_VALIDO = "Il codice scaffale può contenere solo lettere, numeri e trattino (max 20 caratteri)";
    public static final String SCAFFALE_DIMENSIONI_INVALIDE = "Larghezza, profondità e altezza dello scaffale devono essere maggiori di zero";
    public static final String SCAFFALE_ROTAZIONE_INVALIDA = "La rotazione deve essere 0, 90, 180 o 270 gradi";
    public static final String SCAFFALE_GRIGLIA_INVALIDA = "Ripiani e posizioni per ripiano devono essere tra 1 e 50";
    public static final String SCAFFALE_RIDUZIONE_BLOCCATA = "Impossibile ridurre ripiani/posizioni: le seguenti celle contengono articoli: ";
    public static final String SCAFFALE_ELIMINAZIONE_BLOCCATA = "Lo scaffale non può essere eliminato perché alcune celle contengono articoli: ";

    // --- MAGAZZINO POSIZIONI ARTICOLI ---
    public static final String POSIZIONE_DTO_NULL = "I dati della posizione articolo non possono essere null";
    public static final String POSIZIONE_UBICAZIONE_OBBLIGATORIA = "L'ID ubicazione è obbligatorio";
    public static final String POSIZIONE_ARTICOLO_OBBLIGATORIO = "L'ID articolo è obbligatorio";
    public static final String POSIZIONE_QUANTITA_INVALIDA = "La quantità presente non può essere nulla o negativa";
    public static final String POSIZIONE_DUPLICATA = "Articolo già presente nell'ubicazione specificata: ";
    public static final String POSIZIONE_NON_TROVATA = "Posizione articolo non trovata con ID ";

    // --- RICAMBI ---
    public static final String RICAMBIO_DTO_NULL = "I dati del ricambio non possono essere null";
    public static final String RICAMBIO_INTERVENTO_OBBLIGATORIO = "L'ID intervento è obbligatorio";
    public static final String RICAMBIO_ARTICOLO_OBBLIGATORIO = "L'ID articolo è obbligatorio";
    public static final String RICAMBIO_QUANTITA_INVALIDA = "La quantità utilizzata del ricambio deve essere almeno 1";
    public static final String RICAMBIO_PREZZO_NEGATIVO = "Il prezzo unitario del ricambio non può essere negativo";
    public static final String RICAMBIO_NON_TROVATO = "Ricambio non trovato con ID ";

    // --- STORIA NOLEGGI ---
    public static final String NOLEGGIO_DTO_NULL = "I dati del documento noleggio non possono essere null";
    public static final String NOLEGGIO_CLIENTE_OBBLIGATORIO = "Il cliente del noleggio è obbligatorio";
    public static final String NOLEGGIO_ARTICOLO_OBBLIGATORIO = "L'articolo del noleggio è obbligatorio";
    public static final String NOLEGGIO_DATA_OBBLIGATORIA = "La data del noleggio è obbligatoria";
    public static final String NOLEGGIO_MATRICOLA_OBBLIGATORIA = "La matricola del noleggio è obbligatoria e non può essere vuota";
    public static final String NOLEGGIO_MATRICOLA_TOO_LONG = "La matricola supera i 100 caratteri";
    public static final String NOLEGGIO_SEZIONALE_TOO_LONG = "Il sezionale supera i 50 caratteri";
    public static final String NOLEGGIO_ARTICOLO_NON_TROVATO = "Articolo non trovato nel catalogo con ID ";
    public static final String NOLEGGIO_QUANTITA_NEGATIVA = "La quantità del noleggio non può essere negativa";
    public static final String NOLEGGIO_NON_TROVATO = "Documento noleggio non trovato con ID ";
    public static final String NOLEGGIO_PAGINAZIONE_INVALIDA = "La pagina deve essere non negativa e la dimensione deve essere compresa tra 1 e 200";

    // --- MANUTENZIONE NOLEGGI ---
    public static final String MANUTENZIONE_DTO_NULL = "I dati della manutenzione non possono essere null";
    public static final String MANUTENZIONE_CONFIGURAZIONE_INVALIDA = "La configurazione richiede almeno una categoria valida; soglia e numero di ordinarie, se indicati, devono essere positivi";
    public static final String MANUTENZIONE_CONFIGURAZIONE_ASSENTE = "Configurazione manutenzione assente per il noleggio ";
    public static final String MANUTENZIONE_NOLEGGIO_SENZA_CLIENTE = "Il noleggio non è associato a un cliente: ";
    public static final String MANUTENZIONE_NON_DOVUTA = "La manutenzione ordinaria non è ancora dovuta per il noleggio ";
    public static final String MANUTENZIONE_PIANIFICAZIONE_INVALIDA = "La pianificazione richiede giorni di avviso non negativi e periodi con date e intervallo validi";
    public static final String MANUTENZIONE_PERIODI_SOVRAPPOSTI = "I periodi della pianificazione manutentiva non possono sovrapporsi";
    public static final String MANUTENZIONE_PIANO_CLIENTE_NON_TROVATO = "Pianificazione manutenzione non trovata per il cliente ";
    public static final String MANUTENZIONE_PIANO_NON_TROVATO = "Pianificazione manutenzione non trovata per il noleggio ";
    public static final String STATISTICHE_INTERVALLO_INVALIDO = "Il cliente deve essere valido e la data iniziale non può essere successiva alla data finale";

    // --- STORIA PRODOTTI ---
    public static final String PRODOTTO_DTO_NULL = "I dati del prodotto storico non possono essere null";
    public static final String PRODOTTO_CODICE_OBBLIGATORIO = "Il codice articolo è obbligatorio e non può essere vuoto";
    public static final String PRODOTTO_QUANTITA_INVALIDA = "La quantità del prodotto è obbligatoria e non può essere negativa";
    public static final String PRODOTTO_NON_TROVATO = "Record storico prodotto non trovato con ID ";
}
