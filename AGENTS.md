# AGENTS.md — Guida e Linee Guida per Agenti AI

Questo documento descrive le convenzioni architetturali, gli standard di sviluppo, la struttura delle entità e le regole operative del progetto `gestionale-magazzino` (`servizi_pilot_BE`). Qualsiasi agente AI che opera su questa codebase deve seguire scrupolosamente queste linee guida.

---

## 1. Panoramica del Progetto

- **Linguaggio**: Java 21 LTS
- **Framework**: Spring Boot 3.4.1
- **Modulo di Persistenza**: Spring Data JPA + Hibernate ORM 6.6
- **Database di Produzione/Sviluppo**: MySQL 9.0 (`service` su `localhost:3306`)
- **Database di Test**: H2 In-Memory (profilo `create-drop` in `src/test/resources/application.yaml`)
- **Documentazione**: Springdoc OpenAPI (Swagger 3)
- **Tooling**: Maven Wrapper (`mvnw` su Unix, `mvnw.cmd` su Windows)

---

## 2. Architettura e Layering

La codebase segue una rigida separazione a livelli (Layered Architecture):

```
src/main/java/pilot/servizi/
├── config/        # Configurazioni Spring (OpenAPIConfig, etc.)
├── controller/    # REST Controllers con annotazioni OpenAPI e validazione DTO
├── dto/
│   ├── request/   # Java Records immutabili per input client (@Valid, @NotNull, @NotBlank, @Size)
│   └── response/  # Java Records immutabili per risposte JSON
├── entity/        # Entità JPA con mappatura esplicita colonne, tabelle, indici e vincoli
├── log/           # Gestione errori centralizzata (ErrorConstants, GlobalExceptionHandler, ResourceNotFoundException)
├── repository/    # Interfacce Spring Data JpaRepository
└── service/       # Business logic transazionale, validazioni obbligatorie mirate, mapping
```

### Regole per Layer:

1. **Entity**:
   - Mappare sempre esplicitamente la tabella con `@Table(name = "...", indexes = { ... }, uniqueConstraints = { ... })`.
   - Mappare **ogni singola colonna** con `@Column(name = "...", length = ..., nullable = ...)`.
   - Utilizzare le convenzioni snake_case per il database (`ubicazione_id`, `data_doc`, etc.).
   - Attenzione ai tipi:
     - `int` su MySQL ➡️ `Integer` in Java (es. `doc_id` in `storia_noleggi`).
     - `bigint` su MySQL ➡️ `Long` in Java.
     - `date` su MySQL ➡️ `java.time.LocalDate` in Java.
     - `datetime` o `timestamp` su MySQL ➡️ `java.time.LocalDateTime` in Java.
     - `decimal(P, S)` su MySQL ➡️ `java.math.BigDecimal` in Java.
     - `double(P, S)` su MySQL ➡️ `java.lang.Double` in Java.
     - `text` su MySQL ➡️ `String` con `columnDefinition = "TEXT"`.
   - Utilizzare annotazioni Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
   - Gestire le date di modifica/creazione con `@CreationTimestamp` e `@UpdateTimestamp` di Hibernate.

2. **DTO (Request & Response)**:
   - Utilizzare esclusivamente **Java Records** (`public record FooRequest(...)`).
   - Le Request devono contenere le annotazioni di Jakarta Validation (`@NotNull`, `@NotBlank`, `@Size(max = ...)`, `@PositiveOrZero`, `@Min`).
   - Le Response devono esporre solo i dati necessari al client, evitando cicli e riferimenti non serializzabili.

3. **Repository**:
   - Estendere sempre `JpaRepository<Entity, IdType>`.
   - Aggiungere metodi finder con convenzioni standard di Spring Data JPA (`findBy...`, `existsBy...`).
   - Attenzione: **MAI** aggiungere metodi finder per proprietà non esistenti sull'entità JPA (altrimenti il container Spring fallisce all'avvio con `PropertyReferenceException`).

4. **Service**:
   - Annotare la classe con `@Slf4j`, `@Service`, `@RequiredArgsConstructor` e `@Transactional(readOnly = true)`.
   - Metodi di scrittura (`create`, `update`, `delete`, `replace`) annotati esplicitamente con `@Transactional`.
   - **Validazione Mirata Campi Obbligatori**: Ogni metodo `create(...)` e `update(...)` DEVE invocare un metodo privato `validateRequest(dto, operation)` che verifichi:
     - Nullability dell'oggetto request.
     - Stringhe obbligatorie non vuote e senza solo spazi (`dto.field() == null || dto.field().trim().isEmpty()`).
     - Lunghezze massime coerenti con lo schema del DB.
     - Valori numerici nei range ammessi (es. quantità >= 1 o non negativa, importi >= 0).
     - Formato di campi specifici (es. email con `@` e `.`).
     - Esistenza preventiva delle chiavi esterne nei rispettivi repository prima del salvataggio.
     - Unicità dei campi con vincolo univoco (es. `existsByCodice`, `existsByMatricola`).
   - **Logging dello Stato di Errore**: In caso di errore di validazione o mancata corrispondenza, registrare SEMPRE il log di warning tramite `log.warn(...)` utilizzando i template e gli stati definiti in `ErrorConstants` (es. `ErrorConstants.ERR_VALIDATION`, `ErrorConstants.ERR_NOT_FOUND`, `ErrorConstants.ERR_CONFLICT`).
   - Sollevare le eccezioni appropriate:
     - `IllegalArgumentException` per parametri mancanti o errati (HTTP 400).
     - `ResourceNotFoundException` per risorse o FK inesistenti (HTTP 404).
     - `DataIntegrityViolationException` per conflitti di unicità o vincoli d'integrità (HTTP 409).

5. **Gestione Errori Centralizzata (`GlobalExceptionHandler` & `ErrorConstants`)**:
   - La classe `GlobalExceptionHandler` (`@RestControllerAdvice`) intercetta le eccezioni dell'applicazione e le converte nel record standard `ErrorResponse`:
     - `ResourceNotFoundException` ➡️ HTTP 404 Not Found.
     - `DataIntegrityViolationException` ➡️ HTTP 409 Conflict.
     - `IllegalArgumentException`, `IllegalStateException` ➡️ HTTP 400 Bad Request.
     - `MethodArgumentNotValidException` ➡️ HTTP 400 Bad Request (estrazione messaggi di errore specifici dei campi).
     - `Exception` generica ➡️ HTTP 500 Internal Server Error.
   - Tutte le costanti dei messaggi e degli stati di errore risiedono in `ErrorConstants`.

6. **Controller**:
   - Annotare la classe con `@RestController`, `@RequestMapping("/api/...")`, `@Tag` (per Swagger) e `@RequiredArgsConstructor`.
   - Utilizzare `@Valid @RequestBody` per abilitare la validazione automatica dei DTO.
   - Specificare `@ResponseStatus(HttpStatus.CREATED)` per i metodi POST.
   - Specificare `@ResponseStatus(HttpStatus.NO_CONTENT)` per i metodi DELETE.

---

## 3. Gestione del Database e Validazione Schema

- In `application.yaml` è configurato:
  ```yaml
  spring:
    jpa:
      hibernate:
        ddl-auto: validate
  ```
- **IMPORTANTE**: In modalità `validate`, Hibernate esegue un controllo rigoroso tra i tipi Java dell'entità e le colonne MySQL all'avvio dell'applicazione.
- Se viene modificata un'entità, assicurarsi che:
  - Il nome della colonna coincida (`name = "..."`).
  - Il tipo di dato coincida (es. `int` in MySQL ➡️ `Integer` in Java, mai `Long`).
  - La chiave primaria corrisponda (es. `doc_id` in `storia_noleggi` è `int NOT NULL`, non `bigint`).
  - L'eventuale autoincrement sia presente sia su MySQL che con `@GeneratedValue(strategy = GenerationType.IDENTITY)`.

---

## 4. Test e Profilo di Test

- Per consentire l'esecuzione dei test (`mvn test`) in ambienti CI o locali dove MySQL non è attivo:
  - Il file `src/test/resources/application.yaml` sovrascrive il datasource con **H2 in-memory** in modalità `create-drop`.
  - Non inserire credenziali o dipendenze hardcoded da MySQL nei file di test.

---

## 5. Comandi di Verifica per Agenti

Prima di completare qualsiasi modifica:

1. **Compilazione pulita e test**:
   ```powershell
   ./mvnw clean test
   ```
   Deve terminare con `BUILD SUCCESS`.

2. **Verifica avvio applicazione contro MySQL (se attivo)**:
   ```powershell
   ./mvnw spring-boot:run
   ```
   Verificare nei log che Tomcat parta su porta 8080 e che non vi siano `SchemaManagementException` sollevate da Hibernate Validator.
