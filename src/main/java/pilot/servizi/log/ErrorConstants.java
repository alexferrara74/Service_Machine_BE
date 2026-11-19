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

    // --- APPUNTAMENTI ASSISTENZA ---
    public static final String APPUNTAMENTO_DTO_NULL = "I dati dell'appuntamento non possono essere null";
    public static final String APPUNTAMENTO_TITOLO_OBBLIGATORIO = "Il titolo dell'appuntamento è obbligatorio e non può essere vuoto";
    public static final String APPUNTAMENTO_STATO_OBBLIGATORIO = "Lo stato dell'appuntamento è obbligatorio";
    public static final String APPUNTAMENTO_DATE_OBBLIGATORIE = "Data di inizio e di fine dell'appuntamento sono obbligatorie";
    public static final String APPUNTAMENTO_DATE_INCOERENTI = "La fine dell'appuntamento deve essere successiva all'inizio";
    public static final String APPUNTAMENTO_CLIENTE_OBBLIGATORIO = "Senza un noleggio censito il nome del cliente è obbligatorio";
    public static final String APPUNTAMENTO_CAMPO_TOO_LONG = "Un campo dell'appuntamento supera la lunghezza massima consentita: ";
    public static final String APPUNTAMENTO_NON_TROVATO = "Appuntamento non trovato con ID ";
    public static final String APPUNTAMENTO_INTERVALLO_INVALIDO = "L'intervallo del calendario richiede una data iniziale precedente a quella finale";

    // --- CATALOGO PRODOTTI (articoli manuali) ---
    public static final String CATALOGO_DTO_NULL = "I dati dell'articolo non possono essere null";
    public static final String CATALOGO_CODICE_OBBLIGATORIO = "Il codice articolo è obbligatorio e non può essere vuoto";
    public static final String CATALOGO_CODICE_TOO_LONG = "Il codice articolo supera la lunghezza massima di 50 caratteri";
    public static final String CATALOGO_DESCRIZIONE_OBBLIGATORIA = "La descrizione dell'articolo è obbligatoria e non può essere vuota";
    public static final String CATALOGO_DESCRIZIONE_TOO_LONG = "La descrizione dell'articolo supera la lunghezza massima di 255 caratteri";
    public static final String CATALOGO_CODICE_DUPLICATO = "Articolo già presente nel catalogo con codice: ";

    // --- UTENTI E AUTENTICAZIONE ---
    public static final String AUTH_CREDENZIALI_NON_VALIDE = "Credenziali non valide";
    public static final String AUTH_NON_AUTENTICATO = "Autenticazione richiesta: effettua il login";
    public static final String AUTH_NON_AUTORIZZATO = "Non hai i permessi per eseguire questa operazione";
    public static final String AUTH_TROPPI_TENTATIVI = "Troppi tentativi di accesso falliti: riprova tra qualche minuto";
    public static final String UTENTE_DTO_NULL = "I dati dell'utente non possono essere null";
    public static final String UTENTE_USERNAME_NON_VALIDO = "Lo username deve avere da 3 a 50 caratteri tra lettere, numeri, punto, trattino e underscore";
    public static final String UTENTE_USERNAME_DUPLICATO = "Esiste già un utente con username: ";
    public static final String UTENTE_RUOLO_OBBLIGATORIO = "Il ruolo dell'utente è obbligatorio";
    public static final String UTENTE_PASSWORD_DEBOLE = "La password deve avere almeno 10 caratteri (massimo 72) e non può coincidere con lo username";
    public static final String UTENTE_NON_TROVATO = "Utente non trovato con ID ";
    public static final String UTENTE_ULTIMO_ADMIN = "Deve restare almeno un amministratore attivo";
    public static final String UTENTE_AUTO_MODIFICA = "Non puoi cambiare il tuo ruolo, disattivarti o eliminarti: chiedi a un altro amministratore";
    public static final String UTENTE_PASSWORD_ATTUALE_ERRATA = "La password attuale non è corretta";
}
