# Installazione su Windows Server

Il gestionale gira come **un solo servizio Windows** (`ServiziPilot`) più **MySQL**. Il servizio mostra il sito e le API sulla **stessa porta**
(es. `8082`): i client aprono `http://NOME-SERVER:8082` da un browser, senza installare nulla. Non serve Docker.

```
  browser dei client ──► porta 8082 ──► servizio ServiziPilot (Java) ──► MySQL (stesso server, solo locale)
                                          ├─ sito web (cartella C:\ServiziPilot\web)
                                          └─ API  /api/...
```

> **Stato di questa guida:** è stata scritta senza poterla eseguire su un Windows Server né su un MySQL reale. L'applicazione, le
> migrazioni e il servizio del sito sono stati provati su un altro sistema (vedi "Cosa è stato verificato" in fondo); i comandi
> Windows (servizio, firewall, backup) no. Alla prima installazione segui il passo 8 e controlla ogni punto.

## 0. Cosa serve

| | |
|---|---|
| Server | Windows Server 2016 o successivo, 4 GB RAM, 10 GB liberi (foto e PDF vanno nel database), accesso da amministratore |
| Pacchetto | cartella `Pacchetto_Windows` con `app\ServiziPilot.jar`, `web\` e i file di `servizio\` |
| Da scaricare | Java 21, MySQL, WinSW (link ai passi 2, 3, 6). Se il server non ha internet, scaricali da un altro PC e copiali |
| Informazioni da decidere | porta (es. 8082), password del database, password dell'amministratore |

## 1. Cartelle

In PowerShell **come amministratore**:

```powershell
New-Item -ItemType Directory -Force C:\ServiziPilot\app, C:\ServiziPilot\web, C:\ServiziPilot\servizio, C:\ServiziPilot\logs, C:\ServiziPilot\backup, C:\ServiziPilot\config
```

Copia dal pacchetto:

| Da | A |
|---|---|
| `app\ServiziPilot.jar` | `C:\ServiziPilot\app\` |
| contenuto di `web\` (c'è `index.csr.html`) | `C:\ServiziPilot\web\` |
| `servizio\ServiziPilot.xml` | `C:\ServiziPilot\servizio\` |
| `servizio\backup-db.ps1` | `C:\ServiziPilot\` |
| `servizio\crea-database.sql` | `C:\ServiziPilot\config\` |

## 2. Java 21

1. Scarica **Temurin 21 (JDK o JRE), Windows x64, `.msi`** da <https://adoptium.net/temurin/releases/?version=21>.
2. Installa lasciando attive le opzioni **"Set JAVA_HOME variable"** e **"Add to PATH"** (oppure, da PowerShell:
   `msiexec /i OpenJDK21U-jdk_x64_windows_hotspot_*.msi ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJavaHome /qn`).
3. Apri una **nuova** PowerShell e verifica: `java -version` deve dire `21.x`.

## 3. MySQL

Versione: la stessa usata in sviluppo (lo schema è stato creato con **MySQL 9.0**; la 8.4 LTS dovrebbe andare ma non è stata provata).
**MariaDB non è supportata.** Controlla in sviluppo con `SELECT VERSION();`.

1. Scarica **MySQL Installer for Windows** da <https://dev.mysql.com/downloads/installer/> e scegli **Server only**.
2. Nelle schermate: *Config Type* = **Server Computer**; *Authentication* = strong password encryption; imposta la **password di root** (conservala);
   *Windows Service*: lascia il nome proposto, **Start the MySQL Server at System Startup** attivo, account **Standard System Account**.
3. **Non** aprire la porta 3306 nel firewall: il database deve essere raggiungibile solo dal servizio, in locale.
4. Crea database e utente. Apri `C:\ServiziPilot\config\crea-database.sql`, sostituisci `SOSTITUISCI-PASSWORD` con la password che vuoi per l'applicazione
   e salva. Poi, da **cmd** (non PowerShell, che non accetta `<`):

   ```bat
   "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" -u root -p < C:\ServiziPilot\config\crea-database.sql
   ```
5. Verifica: `"C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" -u servizi -p service -e "SELECT 1"` deve stampare `1`.

Le tabelle **non vanno create a mano**: le crea l'applicazione al primo avvio (Flyway, migrazioni V1 → V10).
Se invece devi portare i dati dal database di sviluppo, vedi la sezione "Portare i dati esistenti".

## 4. Porta e indirizzo

Scegli la porta (qui `8082`). Anche i client devono raggiungere il server per nome o IP: se serve un nome stabile, assegna al server un IP fisso o
un nome nel DNS interno. In seguito i client useranno sempre `http://NOME-O-IP:8082`.

## 5. Segreti e configurazione

Apri `C:\ServiziPilot\servizio\ServiziPilot.xml` con un editor di testo e sostituisci:

| Valore | Cosa mettere |
|---|---|
| `DB_PASSWORD` | la password scelta al passo 3 |
| `JWT_SECRET` | testo casuale di **almeno 32 caratteri** (senza, il servizio non parte) |
| `ADMIN_PASSWORD` | password del primo amministratore, **almeno 10 caratteri**; usata solo al primo avvio |
| `SERVER_PORT` | la porta del passo 4 |

Per generare valori casuali, in PowerShell:

```powershell
-join ((48..57)+(65..90)+(97..122) | Get-Random -Count 48 | ForEach-Object {[char]$_})
```

Esegui il comando una volta per `JWT_SECRET` e una per `ADMIN_PASSWORD` (puoi accorciare con `-Count 16` per la password), e **annota la password admin**.
Se in una password compare `&`, `<` o `>` nel file XML vanno scritti `&amp;`, `&lt;`, `&gt;`: con il comando sopra non succede.

## 6. Il servizio Windows (l'"exe")

1. Scarica **`WinSW-x64.exe`** dalla versione 2.12.0 di WinSW: <https://github.com/winsw/winsw/releases/tag/v2.12.0>.
2. Copialo in `C:\ServiziPilot\servizio\` e **rinominalo `ServiziPilot.exe`**. Deve avere lo stesso nome base di `ServiziPilot.xml`.
3. Limita l'accesso al file con le password (solo amministratori e sistema):

   ```powershell
   icacls C:\ServiziPilot\servizio\ServiziPilot.xml /inheritance:r /grant:r "Administrators:F" "SYSTEM:F"
   ```
4. Installa e avvia:

   ```powershell
   cd C:\ServiziPilot\servizio
   .\ServiziPilot.exe install
   .\ServiziPilot.exe start
   .\ServiziPilot.exe status      # deve dire: Started
   ```

Da qui il servizio compare in `services.msc` come **Servizi Pilot**, parte da solo all'avvio del server e si riavvia da solo se si ferma.
Comandi utili: `stop`, `restart`, `uninstall` (stesso `.\ServiziPilot.exe`).

## 7. Firewall

```powershell
New-NetFirewallRule -DisplayName "Servizi Pilot" -Direction Inbound -Protocol TCP -LocalPort 8082 -Action Allow
```

Se il server è in una rete con più reti, aggiungi `-Profile Domain,Private` per non aprire la porta su reti pubbliche.

## 8. Primo avvio e verifica

Il primo avvio sul database vuoto crea le tabelle (qualche secondo). Controlla in ordine:

1. **Log:** `Get-Content C:\ServiziPilot\logs\ServiziPilot.out.log -Tail 40`. Cerca `Successfully applied 10 migrations` e `Started DemoApplication`.
   Se c'è `APPLICATION FAILED TO START`, vedi "Problemi frequenti".
2. **Dal server:** apri `http://localhost:8082` nel browser: appare la pagina di accesso.
3. **Accesso:** utente `admin` (o `ADMIN_USERNAME`), password `ADMIN_PASSWORD`. Subito dopo vai su **Account** e cambia la password.
4. **Da un altro PC** della rete: apri `http://NOME-SERVER:8082`. Se non risponde è quasi sempre il firewall (passo 7) o il nome del server.
5. **Utenti:** da **Utenti** crea gli account dei colleghi: `ADMIN` può modificare tutto, `TECNICO` consulta tutto e negli appuntamenti scrive il resoconto, allega foto, conferma e chiude (per i tablet dei tecnici), `VIEWER` può solo consultare.
6. **Prova di scrittura:** crea un cliente, ricarica la pagina, riavvia il servizio (`.\ServiziPilot.exe restart`) e controlla che il cliente ci sia ancora.

## 9. HTTPS (consigliato se si esce dalla rete locale)

Con `http` la password e il token di accesso viaggiano in chiaro. In una LAN fidata è accettabile; fuori dalla LAN o con Wi-Fi ospiti no.
Con un certificato in formato PKCS12 (`certificato.p12`, anche di una CA interna) aggiungi al file `ServiziPilot.xml`:

```xml
<env name="SERVER_SSL_KEY_STORE" value="file:C:/ServiziPilot/config/certificato.p12"/>
<env name="SERVER_SSL_KEY_STORE_PASSWORD" value="password-del-certificato"/>
<env name="SERVER_SSL_KEY_STORE_TYPE" value="PKCS12"/>
```

poi `.\ServiziPilot.exe restart` e usa `https://NOME-SERVER:8082`. Il nome nel certificato deve coincidere con quello usato dai client.
Dopo il cambio ripeti il comando `icacls` del passo 6 se hai ricreato il file.

## 10. Backup

Foto, PDF e dati stanno **tutti nel database**: basta salvare quello.

1. Crea `C:\ServiziPilot\config\mysql-backup.cnf` con le credenziali dell'utente `servizi`:

   ```ini
   [client]
   user=servizi
   password=LA-PASSWORD-DEL-DATABASE
   ```

   e proteggilo: `icacls C:\ServiziPilot\config\mysql-backup.cnf /inheritance:r /grant:r "Administrators:F" "SYSTEM:F"`
2. Se MySQL non è in `C:\Program Files\MySQL\MySQL Server 9.0\bin`, correggi `$MysqlBin` all'inizio di `backup-db.ps1`.
3. Prova una volta a mano: `powershell -NoProfile -ExecutionPolicy Bypass -File C:\ServiziPilot\backup-db.ps1` e controlla che in `C:\ServiziPilot\backup` ci sia uno `.zip`.
4. Pianificalo ogni notte alle 02:00 (tiene 30 giorni):

   ```powershell
   schtasks /Create /TN "ServiziPilot backup" /SC DAILY /ST 02:00 /RU SYSTEM /RL HIGHEST /TR "powershell -NoProfile -ExecutionPolicy Bypass -File C:\ServiziPilot\backup-db.ps1"
   ```
5. **Copia i backup fuori dal server** (altro PC, NAS, cloud): un backup sullo stesso disco non protegge dalla rottura del disco.

**Ripristino** (su database vuoto o da rimpiazzare): ferma il servizio, scompatta lo `.zip`, poi

```powershell
.\ServiziPilot.exe stop
Expand-Archive C:\ServiziPilot\backup\service_AAAAMMGG_HHMM.sql.zip C:\ServiziPilot\backup\ripristino
& "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" -u root -p -e "DROP DATABASE service; CREATE DATABASE service CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci; USE service; SOURCE C:/ServiziPilot/backup/ripristino/service_AAAAMMGG_HHMM.sql"
.\ServiziPilot.exe start
```

Prova il ripristino **una volta** su un altro database prima di averne bisogno.

## 11. Aggiornare a una nuova versione

1. **Backup** (passo 10, punto 3).
2. `cd C:\ServiziPilot\servizio; .\ServiziPilot.exe stop`
3. Conserva i vecchi file (`Rename-Item C:\ServiziPilot\app\ServiziPilot.jar ServiziPilot.jar.vecchio`), poi copia il nuovo `ServiziPilot.jar` e sostituisci il contenuto di `C:\ServiziPilot\web`.
4. `.\ServiziPilot.exe start`. Le nuove migrazioni del database partono da sole.
5. Sui browser dei client premi **Ctrl+F5** una volta, se vedi ancora la versione vecchia.

Per tornare indietro: `stop`, ripristina il backup del passo 1, rimetti il vecchio `.jar` e il vecchio `web`, `start`.
Non rimettere un vecchio `.jar` su un database già migrato senza ripristinare il backup.

## Portare i dati esistenti (dal database di sviluppo)

Sul PC di sviluppo:

```bat
mysqldump -u root -p --single-transaction --routines --default-character-set=utf8mb4 --result-file=service.sql service
```

Copia `service.sql` sul server e ripristinalo come al passo 10 (sezione Ripristino), **prima** del primo avvio del servizio.
All'avvio le migrazioni mancanti si applicano da sole.

Controllo prima di copiare: `SELECT version, description FROM flyway_schema_history ORDER BY installed_rank;` sul database di sviluppo.
Se compaiono migrazioni V3, V4 o V5 con nomi diversi da `appuntamenti_assistenza`, `origine_clienti`, `utenti` (provengono da rami di lavoro non uniti),
il servizio si ferma con "Migration checksum mismatch": in quel caso **non** copiare quel database, parti da un database vuoto o chiedi di sistemarlo.

## Problemi frequenti

| Sintomo | Causa e rimedio |
|---|---|
| `ServiziPilot.exe status` dice `Stopped` e il log dice `JWT_SECRET` | `JWT_SECRET` mancante o sotto i 32 caratteri (passo 5) |
| `Access denied for user 'servizi'` | password diversa tra `crea-database.sql` e `DB_PASSWORD`; oppure l'utente non è stato creato (passo 3.4) |
| `Communications link failure` / `Connection refused` | MySQL non avviato o non ancora pronto: il servizio riprova da solo ogni 10-60 secondi; controlla `services.msc` |
| `Migration checksum mismatch` | il database contiene migrazioni diverse da quelle del pacchetto: vedi "Portare i dati esistenti" |
| `Web server failed to start. Port 8082 was already in use` | un'altra applicazione usa la porta: `netstat -ano \| findstr :8082`, poi cambia `SERVER_PORT` |
| `java` non riconosciuto durante `install` o `start` | apri una nuova PowerShell dopo aver installato Java, oppure metti il percorso completo di `java.exe` in `<executable>` |
| Pagina bianca o versione vecchia dopo un aggiornamento | Ctrl+F5; se persiste controlla che `web` contenga `index.csr.html` |
| I client non si collegano | firewall (passo 7) e prova prima `http://localhost:8082` sul server |
| Foto o PDF rifiutati | il limite è 20 MB per file; le foto dal telefono vengono ridotte dal sito prima dell'invio |
| Errore nell'XML all'avvio | nei valori `&` va scritto `&amp;`; apri il file in un browser: se è valido lo mostra senza errori |

## Docker

Non è necessario. Su Windows Server i container sono solo Windows: le immagini Linux di MySQL e Java non girano senza strati aggiuntivi (WSL2), che su un server
conviene evitare. La procedura sopra usa solo servizi Windows nativi.

## Disinstallare

```powershell
cd C:\ServiziPilot\servizio; .\ServiziPilot.exe stop; .\ServiziPilot.exe uninstall
Remove-NetFirewallRule -DisplayName "Servizi Pilot"
schtasks /Delete /TN "ServiziPilot backup" /F
```

I dati restano in MySQL e in `C:\ServiziPilot\backup` finché non li elimini tu.

## Cosa è stato verificato (e cosa no)

- **Verificato** su un altro sistema: build del `.jar`, 151 test automatici, avvio dell'applicazione con tutte le migrazioni (V1-V10) applicate su un database
  H2 in modalità MySQL, accesso con login, sito e API serviti dalla stessa porta (anche con ricarica diretta di una pagina interna), `/api/...` ancora protette.
- **Verificato anche** su MySQL **8.4 e 9.1 reali** (container Docker su Linux): le 10 migrazioni, l'avvio con la validazione dello schema e le funzioni principali.
- **Non verificato**: MySQL **per Windows** in particolare (la prima installazione è il banco di prova: controlla il log al passo 8),
  tutti i comandi Windows di questa guida (WinSW, firewall, `icacls`, `schtasks`) e lo script `backup-db.ps1`.
