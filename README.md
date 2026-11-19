# Gestionale Magazzino & Interventi Service (BE)

Backend REST API sviluppato con **Spring Boot 3.4**, **Java 21**, **Spring Data JPA (Hibernate 6)** e **MySQL 9.0**.  
Il servizio gestisce l'anagrafica clienti, parco macchine/attrezzature, interventi tecnici con ricambistica, logistica di magazzino digitale con giacenze, e storico noleggi e prodotti.

---

## 🛠️ Stack Tecnologico

- **Java**: 21
- **Framework**: Spring Boot 3.4.1
- **Persistenza**: Spring Data JPA / Hibernate ORM 6.6.4
- **Database Primario**: MySQL 9.0 (`jdbc:mysql://localhost:3306/service`)
- **Database Test**: H2 In-Memory (compatibilità MySQL)
- **Documentazione API**: Springdoc OpenAPI / Swagger UI 2.7.0
- **Lombok**: Generazione boilerplate (Getter, Setter, Builder, Costruttori)
- **Build Tool**: Apache Maven Wrapper (`./mvnw`)

---

## 🗄️ Architettura Dati (MySQL)

Il database `service` comprende le tabelle relazionali elencate qui sotto:

| Tabella | Entity JPA | Descrizione |
|---|---|---|
| `local_clienti` | [`Clienti`](file:///C:/Users/alexf/Desktop/Servizi_Pilot/servizi_pilot_BE/src/main/java/pilot/servizi/entity/Clienti.java) | Anagrafica clienti sincronizzata con sistema centrale/ERP |
| `local_attrezzature` | [`Attrezzature`](file:///C:/Users/alexf/Desktop/Servizi_Pilot/servizi_pilot_BE/src/main/java/pilot/servizi/entity/Attrezzature.java) | Macchinari e attrezzature con matricola univoca |
| `interventi` | [`Interventi`](file:///C:/Users/alexf/Desktop/Servizi_Pilot/servizi_pilot_BE/src/main/java/pilot/servizi/entity/Interventi.java) | Ticket e schede di intervento tecnico |
| `magazzino_digitale` | [`MagazzinoDigitale`](file:///C:/Users/alexf/Desktop/Servizi_Pilot/servizi_pilot_BE/src/main/java/pilot/servizi/entity/MagazzinoDigitale.java) | Ubicazioni fisiche del magazzino (zona, scaffale, ripiano) |
| `magazzino_posizioni_articoli` | [`MagazzinoPosizioniArticoli`](file:///C:/Users/alexf/Desktop/Servizi_Pilot/servizi_pilot_BE/src/main/java/pilot/servizi/entity/MagazzinoPosizioniArticoli.java) | Giacenze puntuali articoli per singola ubicazione |
| `ricambistica_interventi` | [`Ricambi`](file:///C:/Users/alexf/Desktop/Servizi_Pilot/servizi_pilot_BE/src/main/java/pilot/servizi/entity/Ricambi.java) | Componenti e ricambi consumati durante un intervento |
| `storia_noleggi` | [`StoriaNoleggi`](file:///C:/Users/alexf/Desktop/Servizi_Pilot/servizi_pilot_BE/src/main/java/pilot/servizi/entity/StoriaNoleggi.java) | Storico documenti di noleggio macchinari ai clienti |
| `storia_noleggi_documenti` | `StoriaNoleggioDocumento` | Documento PDF associato al singolo noleggio |
| `storia_prodotti` | [`StoriaProdotti`](file:///C:/Users/alexf/Desktop/Servizi_Pilot/servizi_pilot_BE/src/main/java/pilot/servizi/entity/StoriaProdotti.java) | Storico forniture e consumi merci clienti |
| `local_prodotti` | `ProdottiCatalogo` | Catalogo ERP sincronizzato, incluse le attrezzature |
| `noleggio_manutenzione_configurazioni` | `NoleggioManutenzioneConfigurazione` | Soglia e numero di manutenzioni ordinarie configurati per noleggio |
| `noleggio_manutenzione_categorie` | `NoleggioManutenzioneConfigurazione.categoriaIds` | Categorie prodotto conteggiate per il noleggio |
| `noleggio_manutenzioni` | `NoleggioManutenzione` | Storico manutenzioni e quantità acquistata al momento dell'intervento |

---

## 🌐 Endpoints REST API

Tutti gli endpoint rispondono con payload JSON e restituiscono codici di stato HTTP standard (`200 OK`, `201 CREATED`, `204 NO_CONTENT`, `400 BAD_REQUEST`, `404 NOT_FOUND`, `409 CONFLICT`).

### Sincronizzazione ERP
- `POST /api/sync/erp` - Avvia la sincronizzazione tramite il microservizio ERP.

L'endpoint del microservizio ERP non richiede token; anche la chiamata server-to-server del BE non ne inoltra uno. Configurare `SYNC_SERVICE_URL` se l’endpoint ERP non è raggiungibile all’indirizzo predefinito `http://localhost:3000/api/sincronizza`. Limitare l'esposizione del microservizio a localhost o a una rete interna fidata.

### 1. Clienti (`/api/clienti`)
> `DELETE` risponde `409 CONFLICT` se il cliente ha noleggi, interventi o storico acquisti associati.

- `POST /api/clienti` - Crea cliente
- `GET /api/clienti/{id}` - Dettaglio per ID
- `GET /api/clienti` - Lista completa clienti
- `PUT /api/clienti/{id}` - Aggiorna cliente
- `DELETE /api/clienti/{id}` - Elimina cliente

### 2. Attrezzature (`/api/attrezzature`)
> `DELETE` risponde `409 CONFLICT` se la matricola è usata da noleggi o interventi.

- `POST /api/attrezzature` - Registra nuova attrezzatura
- `GET /api/attrezzature/{id}` - Dettaglio per ID
- `GET /api/attrezzature/matricola/{matricola}` - Ricerca per matricola
- `GET /api/attrezzature` - Lista tutte le attrezzature
- `GET /api/attrezzature/stato/{stato}` - Filtra per stato (`DISPONIBILE`, `IN_USO`, `IN_MANUTENZIONE`)
- `GET /api/attrezzature/marca/{marca}` - Filtra per marca
- `PUT /api/attrezzature/{id}` - Aggiorna dati attrezzatura
- `DELETE /api/attrezzature/{id}` - Elimina attrezzatura

### 2.1 Catalogo ERP (`/api/prodotti/catalogo`)
- `GET /api/prodotti/catalogo` - Elenco catalogo
- `GET /api/prodotti/catalogo?categoriaId=42` - Filtro per categoria
- `GET /api/prodotti/catalogo?attrezzatura=true` - Solo attrezzature
- `GET /api/prodotti/catalogo?attrezzatura=false` - Solo prodotti veri (esclude attrezzature e accessori)
- `GET /api/prodotti/catalogo?accessorio=true` - Solo accessori
- `GET /api/prodotti/catalogo/{articoloId}` - Dettaglio articolo

Il catalogo è di sola lettura dal BE: il microservizio ERP continua a popolare le tabelle condivise. Le righe di `storia_prodotti` restano lo storico delle vendite e alimentano statistiche e prodotti acquistati dei clienti.

### 3. Interventi Tecnici (`/api/interventi`)
- `POST /api/interventi` - Apri nuovo intervento
- `GET /api/interventi/{id}` - Dettaglio intervento
- `GET /api/interventi` - Elenco interventi
- `GET /api/interventi/cliente/{clienteId}` - Elenco interventi per cliente
- `PUT /api/interventi/{id}` - Aggiorna stato/dati intervento
- `DELETE /api/interventi/{id}` - Elimina intervento
- `GET /api/interventi/{id}/ricambi` - Lista ricambi associati all'intervento
- `POST /api/interventi/{id}/ricambi` - Registra ricambio utilizzato
- `DELETE /api/interventi/{id}/ricambi/{ricambioId}` - Rimuove ricambio

### 4. Magazzino Ubicazioni (`/api/magazzino`)
- `POST /api/magazzino` - Crea ubicazione
- `GET /api/magazzino/{id}` - Dettaglio ubicazione
- `GET /api/magazzino` - Lista ubicazioni
- `PUT /api/magazzino/{id}` - Aggiorna ubicazione
- `DELETE /api/magazzino/{id}` - Elimina ubicazione

### 5. Giacenze e Posizioni Articoli (`/api/magazzino/posizioni`)
- `POST /api/magazzino/posizioni` - Alloca articolo in ubicazione
- `GET /api/magazzino/posizioni/{id}` - Dettaglio posizione per ID
- `GET /api/magazzino/posizioni` - Elenco posizioni/giacenze
- `GET /api/magazzino/posizioni/ubicazione/{ubicazioneId}` - Articoli presenti in una ubicazione
- `GET /api/magazzino/posizioni/articolo/{articoloId}` - Ubicazioni occupate da un articolo
- `GET /api/magazzino/posizioni/articolo/{articoloId}/giacenza` - Somma giacenze totali articolo
- `PUT /api/magazzino/posizioni/{id}` - Aggiorna quantità/posizione
- `DELETE /api/magazzino/posizioni/{id}` - Rimuovi articolo da ubicazione

### 6. Storico Noleggi (`/api/storia/noleggi`)
- `POST /api/storia/noleggi` - Registra un noleggio inserito dall'operatore. `docId` è generato dal sistema (non va inviato) e identifica anche il PDF allegato. Obbligatori: `clienteId`, `articoloId`, `dataDoc`, `matricola`; cliente, articolo (catalogo) e matricola (attrezzature) devono esistere
- `GET /api/storia/noleggi/{docId}` - Dettaglio documento
- `GET /api/storia/noleggi` - Lista documenti noleggio
- `GET /api/storia/noleggi/pagina?page=0&size=50` - Lista paginata (dimensione massima 200; ordinamento per `docId` decrescente)
- `GET /api/storia/noleggi/cliente/{clienteId}` - Documenti per cliente
- `GET /api/storia/noleggi/cliente/{clienteId}/pagina?page=0&size=50` - Noleggi paginati per cliente
- `GET /api/storia/noleggi/articolo/{articoloId}` - Noleggi per articolo
- `GET /api/storia/noleggi/articolo/{articoloId}/pagina?page=0&size=50` - Noleggi paginati per articolo
- `GET /api/storia/noleggi/matricola/{matricola}` - Noleggi per matricola
- `GET /api/storia/noleggi/matricola/{matricola}/pagina?page=0&size=50` - Noleggi paginati per matricola
- `PUT /api/storia/noleggi/{docId}` - Aggiorna noleggio
- `DELETE /api/storia/noleggi/{docId}` - Cancella noleggio
- `GET /api/storia/noleggi/{docId}/documento` - Metadati del documento allegato
- `POST /api/storia/noleggi/{docId}/documento` - Carica o sostituisce il PDF (massimo 20 MB, campo multipart `file`)
- `GET /api/storia/noleggi/{docId}/documento/download` - Scarica il PDF allegato
- `GET /api/storia/noleggi/{docId}/manutenzione` - Stato della manutenzione a calendario e stima degli acquisti del cliente
- `PUT /api/storia/noleggi/{docId}/manutenzione/configurazione` - Seleziona le categorie prodotto da usare nelle stime cliente del noleggio (soglia e conteggio ordinario legacy sono facoltativi e non determinano più le scadenze)
- `GET /api/storia/noleggi/{docId}/manutenzione/eventi` - Elenco manutenzioni registrate
- `POST /api/storia/noleggi/{docId}/manutenzione/eventi` - Registra la manutenzione ordinaria quando dovuta
- `POST /api/storia/noleggi/{docId}/manutenzione/eventi/straordinaria` - Registra un intervento straordinario per guasto o segnalazione

I noleggi non provengono dall'ERP: il microservizio di integrazione non li sincronizza, quindi sono gestiti interamente dall'operatore. Per rendere `doc_id` auto-incrementale applicare `src/main/resources/db/manual/20261103_noleggi_gestione_operatore.sql`.

Il contratto PDF è unico per ogni noleggio: un nuovo caricamento sostituisce il precedente. I file vengono conservati in `storia_noleggi_documenti`; sono accettati PDF validi fino a 20 MB.

Per il catalogo ERP applicare, se non già eseguita, la migrazione `migrations/001_catalogo_prodotti.sql` del microservizio di integrazione: crea `local_prodotti` e aggiunge le chiavi ERP necessarie allo storico fatture e alle attrezzature. Per gli allegati noleggio applicare manualmente `src/main/resources/db/manual/20261102_catalogo_documenti_noleggio.sql` al database `service`. Eseguire le migrazioni dopo un backup.

La configurazione del calendario ordinario e dei giorni di preavviso è condivisa da tutte le attrezzature a noleggio di un cliente. Le singole scadenze e gli interventi restano associati al noleggio/macchina. Esempio:

`PUT /api/clienti/{clienteId}/manutenzione/pianificazione`

```json
{
  "giorniAvviso": 7,
  "periodi": [
    { "dataInizio": "2026-06-01", "dataFine": "2026-08-31", "intervalloGiorni": 30 },
    { "dataInizio": "2026-09-01", "dataFine": "2027-05-31", "intervalloGiorni": 90 }
  ]
}
```

Ogni periodo ha date inclusive e non può sovrapporsi a un altro periodo del cliente. `giorniAvviso` indica con quanti giorni di anticipo lo stato diventa `IN_SCADENZA`; non è la frequenza della manutenzione. `intervalloGiorni` è invece la frequenza stagionale. La stessa pianificazione viene applicata a tutti i noleggi del cliente.

Per ogni noleggio l'intervallo applicabile è quello del periodo che comprende la data dell'ultima manutenzione ordinaria di quella macchina; se non esistono interventi ordinari, si usa la data del documento di noleggio. Se tale data precede il primo periodo configurato, il primo intervallo viene calcolato dall'inizio di quel periodo. Se manca una data di riferimento o un periodo applicabile, lo stato è `NON_CONFIGURATA`. Le manutenzioni straordinarie non modificano la data di riferimento ordinaria. Gli acquisti non spostano la scadenza.

La configurazione cliente è leggibile con `GET /api/clienti/{clienteId}/manutenzione/pianificazione`. Se non è stata ancora creata, l'endpoint risponde `404`; il client può proporre valori iniziali ma il calendario si attiva solo dopo il salvataggio.

Esempio per associare i prodotti monitorati per una macchina:

```json
{
  "categoriaIds": [4, 7]
}
```

I campi `sogliaQuantita` e `ordinariePerStraordinaria` restano accettati per compatibilità con i client esistenti, ma non influiscono sulle scadenze. Le manutenzioni straordinarie si registrano per segnalazione e non modificano la data di scadenza ordinaria.

### Statistiche cliente (`/api/statistiche/clienti/{clienteId}`)
- `GET /api/statistiche/clienti/{clienteId}?dal=2026-01-01&al=2026-12-31` - Elenco noleggi, acquisti aggregati per prodotto/categoria e andamento mensile.

Gli acquisti sono statistiche aggregate per cliente; non permettono di attribuire i consumi a una singola macchina. Le stime per noleggio selezionano le categorie impostate nella configurazione di quel noleggio ma riportano i totali acquistati dal cliente nel periodo, con ambito `TOTALE_CLIENTE_NON_ATTRIBUITO_A_SINGOLA_MACCHINA`. Se più macchine condividono una categoria, il valore stimato può comparire su più noleggi e non va sommato per ottenere un consumo macchina. Lo storico prodotti non contiene prezzi/importi di vendita, quindi l'API espone quantità e numero di righe, non fatturato.

> **Storico:** da quando c'è Flyway queste migrazioni sono incluse nella V1 e non vanno più applicate a mano. Le istruzioni seguenti valgono solo per chi ha un database parziale da riallineare. Applicare una volta, in ordine, [20261026_manutenzione_noleggi.sql](./src/main/resources/db/manual/20261026_manutenzione_noleggi.sql), [20261027_indici_noleggi.sql](./src/main/resources/db/manual/20261027_indici_noleggi.sql), [20261028_manutenzione_stagionale.sql](./src/main/resources/db/manual/20261028_manutenzione_stagionale.sql) e [20261029_manutenzione_cliente.sql](./src/main/resources/db/manual/20261029_manutenzione_cliente.sql) al database MySQL prima di avviare il backend con `ddl-auto: validate`.

La migrazione 20261029 crea la configurazione condivisa per cliente ma non copia automaticamente i vecchi calendari per noleggio: potrebbero contenere intervalli diversi per lo stesso cliente. Le vecchie tabelle vengono lasciate intatte; dopo la migrazione impostare una pianificazione nella scheda cliente. Gli eventi manutentivi esistenti, invece, restano associati ai rispettivi noleggi e continuano a fornire il riferimento della prossima scadenza.
Per evitare di trasferire tutto lo storico, usare i nuovi endpoint `/pagina`; gli endpoint esistenti senza paginazione restano disponibili per compatibilità.

### 7. Storico Prodotti (`/api/storia/prodotti`)
- `POST /api/storia/prodotti` - Registra riga storico prodotti
- `GET /api/storia/prodotti/{id}` - Dettaglio riga storico
- `GET /api/storia/prodotti` - Elenco storico prodotti
- `GET /api/storia/prodotti/cliente/{clienteId}` - Storico acquisti per cliente
- `GET /api/storia/prodotti/articolo/{articoloId}` - Storico per articolo
- `GET /api/storia/prodotti/codice/{codiceArticolo}` - Storico per codice articolo
- `PUT /api/storia/prodotti/{id}` - Aggiorna riga storico
- `DELETE /api/storia/prodotti/{id}` - Elimina voce storico

### 8. Sincronizzazione (`/api/sync`)
- `POST /api/sync/intervento` - Sincronizza testata intervento e lista ricambi in una singola transazione atomica.

---

## 🔒 Regole di Validazione e Controlli di Business nei Service

Tutti i service applicano controlli preventivi a livello applicativo prima dell'interazione con il database:

1. **Campi Obbligatori**:
   - `ClientiService`: `codice` e `ragioneSociale` obbligatori e non vuoti.
   - `AttrezzaturaService`: `matricola` obbligatoria e non vuota.
   - `InterventiService`: `clienteId`, `matricolaAttrezzatura`, `titolo`, `stato`, `dataApertura` obbligatori.
   - `MagazzinoDigitaleService`: `codiceUbicazione`, `zona` obbligatori.
   - `MagazzinoPosizioniArticoliService`: `ubicazioneId`, `articoloId`, `quantitaPresente` (non negativa) obbligatori.
   - `RicambisticaInterventiService`: `interventoId`, `articoloId`, `quantitaUtilizzata` (>= 1), `prezzoUnitario` (>= 0).
   - `StoriaNoleggiService`: `docId` obbligatorio, `quantita` non negativa.
   - `StoriaProdottiService`: `codiceArticolo`, `quantita` (non negativa) obbligatori.

2. **Integrità Referenziale e Vincoli Esterni**:
   - Negli interventi, viene verificata l'esistenza sia del cliente in `local_clienti` che dell'attrezzatura in `local_attrezzature` tramite matricola.
   - Nei ricambi, viene verificata la presenza dell'intervento prima dell'inserimento.
   - Nelle posizioni magazzino, viene verificata l'esistenza dell'ubicazione e l'unicità della coppia `(ubicazione_id, articolo_id)`.
   - Nello storico noleggi e prodotti, se specificato il `clienteId`, viene validata la sua esistenza.

3. **Integrità oltre le FK (che in alcune tabelle non esistono)**:
   - `AttrezzaturaService`: lo `stato` deve essere `DISPONIBILE`, `IN_USO` o `IN_MANUTENZIONE` (maiuscole/minuscole indifferenti, vuoto = `DISPONIBILE`); la matricola non si modifica se è usata da noleggi o interventi (`409`).
   - `MagazzinoDigitaleService`: un'ubicazione con giacenze non si elimina (`409`).
   - Ricambi e posizioni magazzino richiedono un `articoloId` presente nel catalogo (`404`), come i noleggi.
   - I campi di testo opzionali vuoti o di soli spazi vengono salvati come `NULL`; gli altri sono ripuliti dagli spazi.
   - Un decimale su un campo intero (es. quantità `2.5`) è rifiutato con `400` che indica il campo, non troncato.
   - `POST /api/sync/erp`: una sola sincronizzazione alla volta, la seconda riceve `409`.

4. **Coerenza Logica**:
   - Negli interventi, se fornita `dataChiusura`, viene verificato che non sia cronologicamente antecedente a `dataApertura`.

---

## 🚀 Avvio e Test

### Prerequisiti
- JDK 21+
- MySQL Server 9.x in esecuzione sulla porta standard `3306` con database `service` e credenziali `root` / `root`.

### Avvio dell'Applicazione
```bash
./mvnw spring-boot:run
```
L'applicazione avvia Tomcat sulla porta `8082`.
All'avvio Flyway applica le migrazioni (vedi sotto) e poi Hibernate verifica l'aderenza delle entità JPA con lo schema MySQL tramite `ddl-auto: validate`.

### Configurazione (variabili d'ambiente)

I valori predefiniti servono solo allo sviluppo locale; negli altri ambienti vanno sovrascritti.

| Variabile | Predefinito | Uso |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/service?...` | URL JDBC del database |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `root` | Credenziali del database |
| `SYNC_SERVICE_URL` | `http://localhost:3000/api/sincronizza` | Endpoint del microservizio di integrazione |
| `SYNC_API_TOKEN` | vuoto | Se valorizzato è inviato come `Bearer` al microservizio (che deve avere lo stesso `SYNC_API_TOKEN`) |
| `FLYWAY_ENABLED` | `true` | `false` per disattivare le migrazioni automatiche |
| `JWT_SECRET` | **nessuno (obbligatoria)** | Segreto per firmare i token: almeno 32 caratteri casuali |
| `JWT_TTL_MINUTES` | `480` | Durata del token in minuti |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / generata | Primo amministratore, creato solo se non esistono utenti |
| `SERVER_PORT` | `8082` | Porta su cui ascolta il servizio |
| `WEB_DIR` | vuoto | Cartella del frontend compilato (`dist/.../browser`): se valorizzata il BE serve anche il sito dalla stessa porta. Vuoto in sviluppo (`ng serve`) |

**Installazione su Windows Server** (servizio Windows, MySQL, backup, aggiornamenti): [`deploy/windows/INSTALLAZIONE.md`](deploy/windows/INSTALLAZIONE.md).

Il log SQL con i parametri (che possono contenere dati dei clienti) è spento di default; per attivarlo in sviluppo: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.

**Sicurezza:** l'accesso a `/api/**` richiede un login con token JWT e due ruoli (`ADMIN` e `VIEWER`): vedi «Utenti e ruoli» più sotto. `JWT_SECRET` è obbligatoria; senza non parte. Esporre comunque il servizio solo su HTTPS (o dietro un proxy che lo termini): il token viaggia a ogni richiesta.

### Migrazioni database (Flyway)

Lo schema è versionato in `src/main/resources/db/migration` (`V1__schema_iniziale.sql`, poi `V2__…`):

- **Database vuoto:** Flyway crea l'intero schema.
- **Database esistente** (senza storico Flyway): Flyway crea `flyway_schema_history` e fa il *baseline* alla V1 **senza eseguire né modificare nulla**. Prima di farlo accertarsi che il database sia già allo stato della V1; è la stessa condizione richiesta da `ddl-auto: validate`, che altrimenti impedirebbe l'avvio.
- Ogni modifica futura dello schema (anche per l'integrazione) si aggiunge qui come nuova `V<n>__descrizione.sql`; non si modificano le migrazioni già applicate.
- `V2__stati_attrezzature.sql` normalizza gli stati salvati dal vecchio form FE ("Noleggiata", "In Manutenzione") nei valori documentati.
- Gli script in `db/manual/` e in `Integrazione_sana_breakfast/migrations/` sono **storici**: la V1 li consolida. Non vanno più eseguiti su un database nuovo.
- Flyway avvisa che MySQL 9.0 è più recente della sua versione testata: funziona (verificato su MySQL 9.0.1), ma conviene aggiornare Flyway quando disponibile una versione che lo supporti ufficialmente.

### Esecuzione della Suite di Test
```bash
./mvnw clean test
```
I test utilizzano un database H2 in memoria isolato configurato in `src/test/resources/application.yaml` (Flyway è disattivato nel profilo di test: le migrazioni sono per MySQL). Coprono service (Mockito), repository (H2), proxy di sincronizzazione e il comportamento degli errori HTTP (`ErrorHandlingWebTest`).

### Formato degli errori
Tutti gli errori hanno il corpo `{timestamp, status, error, message, path}`: `400` validazione o parametri/JSON non validi, `404` risorsa o percorso inesistente, `405` metodo non supportato, `409` conflitto di unicità o di integrità (i messaggi dei vincoli del database non vengono esposti), `413` PDF oltre 20 MB, `502` microservizio di sincronizzazione non raggiungibile, `500` errore imprevisto (messaggio generico, dettaglio solo nei log).

### Documentazione Swagger UI
Una volta avviata l'applicazione, aprire il browser su:
```
http://localhost:8082/swagger-ui.html
```
Specifica OpenAPI in formato JSON:
```
http://localhost:8082/v3/api-docs
```

## Clienti, articoli e attrezzature: importati dall'ERP o creati a mano

Clienti (`local_clienti`), catalogo (`local_prodotti`) e attrezzature (`local_attrezzature`) hanno la colonna `origine`: `ERP` per le righe importate dal microservizio, `MANUALE` per quelle create dall'operatore (anche dal form del noleggio). Le risposte API la espongono.

- `POST /api/prodotti/catalogo` registra a mano un articolo (tipicamente un'attrezzatura che l'ERP non ha); il resto del catalogo resta di sola lettura.
- `local_clienti.id` e `local_prodotti.articolo_id` non sono auto-increment (li assegna l'ERP): per le righe manuali il service assegna l'id a partire da **1.000.000.000**, così non collidono con gli id dell'ERP. Presupposto da verificare: gli id ERP restano sotto il miliardo.
- La migrazione `V4__origine_clienti.sql` aggiunge `origine` a `local_clienti` con default `ERP`: il microservizio ERP, che non conosce la colonna, continua a scrivere righe `ERP`.

## Utenti e ruoli

L'accesso a `/api/**` richiede un token JWT (`Authorization: Bearer …`) ottenuto con `POST /api/auth/login`.

| Ruolo | Cosa può fare |
|---|---|
| `ADMIN` | Tutto: letture, scritture, sincronizzazione ERP, gestione utenti (`/api/utenti`) |
| `VIEWER` | Solo letture (`GET`) ed esportazioni; ogni altra chiamata riceve `403`. Può cambiare la propria password |

Il controllo è lato server: nascondere i pulsanti nel frontend è solo comodità. Il ruolo si rilegge dal database a ogni richiesta, quindi disattivare o declassare un utente ha effetto subito; il cambio password invalida i token emessi prima.

### Configurazione (variabili d'ambiente)
- `JWT_SECRET` — **obbligatoria**: almeno 32 caratteri casuali. Senza, l'applicazione non parte. Nel profilo `dev` c'è un valore fisso solo per lo sviluppo locale.
- `JWT_TTL_MINUTES` — durata del token, 480 (8 ore) di default.
- `ADMIN_USERNAME` (default `admin`) e `ADMIN_PASSWORD` — usate solo al primo avvio, quando la tabella `utenti` è vuota, per creare il primo amministratore. Se `ADMIN_PASSWORD` manca viene generata una password casuale scritta **una sola volta** nel log: annotala e cambiala subito.
- La migrazione `V5__utenti.sql` crea la tabella `utenti` (password solo come hash BCrypt).

Regole: password di almeno 10 caratteri; non si può eliminare, disattivare o declassare se stessi né l'ultimo amministratore attivo; dopo 5 login falliti l'utente è bloccato per 5 minuti (contatore in memoria, si azzera al riavvio).

### Registro attività
La tabella `registro_attivita` (migrazione `V6`) tiene, in sola aggiunta, chi ha fatto cosa e quando: accessi (riusciti, falliti, bloccati), cambi password, creazioni, modifiche, eliminazioni ed esportazioni, **comprese le richieste negate** (es. un viewer che prova a scrivere). Ogni riga ha utente, ruolo, azione, risorsa e id, esito (`OK`, `NEGATO`, `RIFIUTATO`, `ERRORE`), codice HTTP e indirizzo IP.

- Non contiene i dati inviati né le password; di un login fallito si conserva lo username solo se ha la forma di uno username (chi sbaglia può aver scritto la password nel campo).
- Consultabile solo dagli admin: `GET /api/registro` (filtri `username`, `azione`, `esito`, `da`, `a`, paginato) e pagina "Registro attività" nel frontend. Nessun endpoint lo modifica o lo svuota.
- L'IP è quello visto dal server: dietro un reverse proxy è quello del proxy. Gli indirizzi sono dati personali: decidi per quanto conservare il registro (non c'è una pulizia automatica).
- Un errore di scrittura nel registro viene solo loggato e non blocca l'operazione dell'utente.
