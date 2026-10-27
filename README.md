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
| `storia_prodotti` | [`StoriaProdotti`](file:///C:/Users/alexf/Desktop/Servizi_Pilot/servizi_pilot_BE/src/main/java/pilot/servizi/entity/StoriaProdotti.java) | Storico forniture e consumi merci clienti |
| `noleggio_manutenzione_configurazioni` | `NoleggioManutenzioneConfigurazione` | Soglia e numero di manutenzioni ordinarie configurati per noleggio |
| `noleggio_manutenzione_categorie` | `NoleggioManutenzioneConfigurazione.categoriaIds` | Categorie prodotto conteggiate per il noleggio |
| `noleggio_manutenzioni` | `NoleggioManutenzione` | Storico manutenzioni e quantità acquistata al momento dell'intervento |

---

## 🌐 Endpoints REST API

Tutti gli endpoint rispondono con payload JSON e restituiscono codici di stato HTTP standard (`200 OK`, `201 CREATED`, `204 NO_CONTENT`, `400 BAD_REQUEST`, `404 NOT_FOUND`, `409 CONFLICT`).

### 1. Clienti (`/api/clienti`)
- `POST /api/clienti` - Crea cliente
- `GET /api/clienti/{id}` - Dettaglio per ID
- `GET /api/clienti` - Lista completa clienti
- `PUT /api/clienti/{id}` - Aggiorna cliente
- `DELETE /api/clienti/{id}` - Elimina cliente

### 2. Attrezzature (`/api/attrezzature`)
- `POST /api/attrezzature` - Registra nuova attrezzatura
- `GET /api/attrezzature/{id}` - Dettaglio per ID
- `GET /api/attrezzature/matricola/{matricola}` - Ricerca per matricola
- `GET /api/attrezzature` - Lista tutte le attrezzature
- `GET /api/attrezzature/stato/{stato}` - Filtra per stato (`DISPONIBILE`, `IN_USO`, `IN_MANUTENZIONE`)
- `GET /api/attrezzature/marca/{marca}` - Filtra per marca
- `PUT /api/attrezzature/{id}` - Aggiorna dati attrezzatura
- `DELETE /api/attrezzature/{id}` - Elimina attrezzatura

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
- `POST /api/storia/noleggi` - Registra documento noleggio
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
- `GET /api/storia/noleggi/{docId}/manutenzione` - Stato della manutenzione a calendario e stima degli acquisti del cliente
- `PUT /api/storia/noleggi/{docId}/manutenzione/configurazione` - Seleziona le categorie prodotto da usare nelle stime cliente del noleggio (soglia e conteggio ordinario legacy sono facoltativi e non determinano più le scadenze)
- `GET /api/storia/noleggi/{docId}/manutenzione/eventi` - Elenco manutenzioni registrate
- `POST /api/storia/noleggi/{docId}/manutenzione/eventi` - Registra la manutenzione ordinaria quando dovuta
- `POST /api/storia/noleggi/{docId}/manutenzione/eventi/straordinaria` - Registra un intervento straordinario per guasto o segnalazione

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

Le nuove tabelle non vengono create automaticamente: applicare una volta, in ordine, [20261026_manutenzione_noleggi.sql](./src/main/resources/db/manual/20261026_manutenzione_noleggi.sql), [20261027_indici_noleggi.sql](./src/main/resources/db/manual/20261027_indici_noleggi.sql), [20261028_manutenzione_stagionale.sql](./src/main/resources/db/manual/20261028_manutenzione_stagionale.sql) e [20261029_manutenzione_cliente.sql](./src/main/resources/db/manual/20261029_manutenzione_cliente.sql) al database MySQL prima di avviare il backend con `ddl-auto: validate`.

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

3. **Coerenza Logica**:
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
L'applicazione avvierà Tomcat sulla porta `8080`.  
Hibernate verificherà l'aderenza delle entità JPA con lo schema MySQL tramite `ddl-auto: validate`.

### Esecuzione della Suite di Test
```bash
./mvnw clean test
```
I test utilizzano un database H2 in memoria isolato configurato in `src/test/resources/application.yaml`.

### Documentazione Swagger UI
Una volta avviata l'applicazione, aprire il browser su:
```
http://localhost:8080/swagger-ui.html
```
Specifica OpenAPI in formato JSON:
```
http://localhost:8080/v3/api-docs
```
