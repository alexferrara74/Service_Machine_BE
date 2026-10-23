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

Il database `service` comprende 8 tabelle relazionali:

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
- `GET /api/storia/noleggi/cliente/{clienteId}` - Documenti per cliente
- `GET /api/storia/noleggi/articolo/{articoloId}` - Noleggi per articolo
- `GET /api/storia/noleggi/matricola/{matricola}` - Noleggi per matricola
- `PUT /api/storia/noleggi/{docId}` - Aggiorna noleggio
- `DELETE /api/storia/noleggi/{docId}` - Cancella noleggio

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
