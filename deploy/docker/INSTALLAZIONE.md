# Installazione su Linux con Docker (VM VirtualBox con Linux Mint, o qualunque server Linux)

Due container gestiti da Docker Compose: **MySQL** e l'**applicazione** (API e sito sulla stessa porta, es. `8082`).
I client aprono `http://IP-DEL-SERVER:8082` da un browser, senza installare nulla.

```
  browser dei client ──► porta 8082 ──► container "app" (Java) ──► container "db" (MySQL, mai esposto alla rete)
                                          ├─ sito web (cartella ./web)            └─ dati nel volume Docker "db-data"
                                          └─ API  /api/...
```

Tutto riparte da solo dopo un riavvio della VM (Docker parte con il sistema, i container hanno `restart: unless-stopped`).

## 0. Cosa serve

| | |
|---|---|
| Server | Linux Mint 22 (basata su Ubuntu 24.04; va bene anche la 21), 2 CPU, 4 GB RAM, 25 GB di disco (foto e PDF stanno nel database) |
| Rete | accesso a internet per scaricare Docker e le immagini (circa 1 GB la prima volta) |
| Pacchetto | `Pacchetto_Linux_Docker.zip`: `docker-compose.yml`, `Dockerfile`, `.env.example`, `backup-db.sh`, `app/ServiziPilot.jar`, `web/` |
| Da decidere | porta (es. 8082) e tre password: database root, database applicazione, primo amministratore |

## 1. La VM in VirtualBox (salta questo passo se usi un server fisico)

1. **Nuova VM**: tipo *Linux*, versione *Ubuntu (64-bit)*, **2 CPU, 4096 MB di RAM, disco 30 GB** (allocazione dinamica va bene).
2. **Rete → Scheda 1 → "Connessa a: Scheda con bridge"** (*Bridged adapter*), scegliendo la scheda di rete reale del PC.
   Con la modalità predefinita NAT i colleghi **non** raggiungono la VM.
3. Installa Linux Mint dall'ISO (con il disco cifrato **no**: ad ogni riavvio chiederebbe la password e il servizio resterebbe fermo).
4. In Mint: **Impostazioni → Risparmio energetico → "Sospendi" su "Mai"** (anche per lo schermo bloccato): una VM sospesa è un gestionale spento.
5. **IP fisso:** la soluzione più semplice è una *prenotazione DHCP* sul router per l'indirizzo MAC della VM, così i client usano sempre lo stesso indirizzo.
6. **Avvio con il PC:** VirtualBox non avvia le VM da solo. Su un PC Windows crea un'attività pianificata "all'avvio" che esegue
   `"C:\Program Files\Oracle\VirtualBox\VBoxManage.exe" startvm "NOME-VM" --type headless` con lo stesso utente che ha creato la VM.
   Se il PC si riavvia o va in sospensione, il gestionale è irraggiungibile finché la VM non riparte: per l'uso quotidiano valuta un computer sempre acceso.

## 2. Aggiornare il sistema e installare Docker

Da terminale:

```bash
sudo apt update && sudo apt full-upgrade -y
sudo apt install -y ca-certificates curl gnupg unzip

# Docker Engine dal repository ufficiale
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

Esci dalla sessione e rientra (serve al gruppo `docker`), poi verifica: `docker run --rm hello-world` deve stampare "Hello from Docker!".

> **Attenzione su Linux Mint:** non usare `lsb_release -cs` per il nome della versione: su Mint restituisce il nome di Mint (es. `wilma`), che il repository Docker non conosce.
> Il comando sopra usa `UBUNTU_CODENAME`, che è quello giusto.

## 3. Copiare il pacchetto e configurare

Porta `Pacchetto_Linux_Docker.zip` sulla VM (cartella condivisa di VirtualBox, `scp` o chiavetta USB), poi:

```bash
unzip Pacchetto_Linux_Docker.zip -d /tmp
sudo mv /tmp/Pacchetto_Linux_Docker /opt/servizi-pilot
sudo chown -R "$USER": /opt/servizi-pilot
cd /opt/servizi-pilot
cp .env.example .env
chmod 600 .env
nano .env
```

Nel file `.env` compila i valori lasciati vuoti (le password le puoi generare con `openssl rand -base64 36 | tr -d '/+=' | cut -c1-40`, una volta per ciascuna):

| Variabile | Cosa mettere |
|---|---|
| `MYSQL_ROOT_PASSWORD` | password di root del database (serve solo agli amministratori) |
| `DB_PASSWORD` | password con cui l'applicazione si collega al database |
| `JWT_SECRET` | testo casuale di **almeno 32 caratteri** (senza, l'applicazione non parte) |
| `ADMIN_PASSWORD` | password del primo amministratore, **almeno 10 caratteri**; usata solo al primo avvio |
| `HTTP_PORT` | la porta per i client (predefinita 8082) |

**Conserva il file `.env` in un posto sicuro fuori dal server**: senza le password non si riesce ad amministrare il database.
Il file non va mai su GitHub.

## 4. Avviare

```bash
cd /opt/servizi-pilot
docker compose up -d --build
docker compose ps
docker compose logs -f app        # Ctrl+C per uscire
```

La prima volta scarica le immagini e crea il database. Nei log cerca `Successfully applied 10 migrations` e `Started DemoApplication`.
Se compare `APPLICATION FAILED TO START`, vedi "Problemi frequenti".

## 5. Verificare

1. Dalla VM: `curl -I http://localhost:8082` deve rispondere `200`.
2. Da **un altro PC** della rete: `http://IP-DELLA-VM:8082`. L'indirizzo della VM lo vedi con `hostname -I`.
   Se non risponde: il firewall (passo 6) o la modalità di rete della VM (passo 1.2).
3. Accedi con `admin` e la password scelta, poi vai su **Account** e cambiala.
4. Da **Utenti** crea gli account: `ADMIN` modifica tutto, `TECNICO` (per i tablet) consulta tutto e negli appuntamenti scrive il resoconto,
   allega foto, conferma e chiude, `VIEWER` solo consulta.
5. **Prova di riavvio:** crea un cliente, esegui `sudo reboot`, e a macchina riavviata controlla che il sito risponda e il cliente ci sia ancora, senza lanciare nulla a mano.

## 6. Firewall

Su Linux Mint il firewall (`ufw`) è installato ma spento. Se lo attivi, apri solo la porta del sito:

```bash
sudo ufw allow 8082/tcp
```

Il database **non** ha porte pubblicate: è raggiungibile solo dall'applicazione. Non pubblicarle mai
(i container pubblicati da Docker aggirano `ufw`: una porta 3306 aperta resterebbe aperta anche con il firewall attivo).

## 7. HTTPS (consigliato fuori da una LAN fidata)

Con `http` password e token viaggiano in chiaro: in una LAN fidata è accettabile, fuori no. Con un certificato PKCS12
(`certificato.p12`) metti il file in `/opt/servizi-pilot/` e aggiungi al servizio `app` in `docker-compose.yml`:

```yaml
    volumes:
      - ./web:/web:ro
      - ./certificato.p12:/config/certificato.p12:ro
    environment:
      SERVER_SSL_KEY_STORE: file:/config/certificato.p12
      SERVER_SSL_KEY_STORE_PASSWORD: ${SSL_PASSWORD}
      SERVER_SSL_KEY_STORE_TYPE: PKCS12
```

(le variabili vanno unite a quelle già presenti, non ripetute). *Questa parte non è stata provata.*

## 8. Backup

Foto, PDF e dati stanno **tutti nel database**: basta salvare quello.

```bash
cd /opt/servizi-pilot
chmod +x backup-db.sh
./backup-db.sh                        # crea backup/service_AAAAMMGG_HHMM.sql.gz
```

Pianificalo ogni notte alle 02:00 (tiene 30 giorni): `crontab -e` e aggiungi

```
0 2 * * * /opt/servizi-pilot/backup-db.sh >> /opt/servizi-pilot/backup/backup.log 2>&1
```

**Copia i backup fuori dalla VM** (altro PC, NAS, cloud): un backup sullo stesso disco non protegge dalla rottura del disco.
Lo *snapshot* di VirtualBox non sostituisce il backup del database.

**Ripristino** (sostituisce tutti i dati attuali con quelli del backup):

```bash
cd /opt/servizi-pilot
set -a; . ./.env; set +a
docker compose stop app
docker compose exec -T -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" db mysql -uroot -e "DROP DATABASE service; CREATE DATABASE service CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
gunzip -c backup/service_AAAAMMGG_HHMM.sql.gz | docker compose exec -T -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" db mysql -uroot service
docker compose start app
```

Prova il ripristino **una volta** su una VM di prova prima di averne bisogno.

## 9. Aggiornare a una nuova versione

1. **Backup** (passo 8).
2. Sostituisci `app/ServiziPilot.jar` e il contenuto della cartella `web/` con quelli del nuovo pacchetto.
3. `docker compose up -d --build`. Le nuove migrazioni del database partono da sole.
4. Sui browser dei client premi **Ctrl+F5** una volta, se vedi ancora la versione vecchia.

Per tornare indietro: rimetti il vecchio `.jar` e il vecchio `web`, ripristina il backup del punto 1 e ripeti `docker compose up -d --build`.
Non rimettere un vecchio `.jar` su un database già migrato senza ripristinare il backup.

## Portare i dati dal database di sviluppo

Sul PC di sviluppo: `mysqldump -u root -p --single-transaction --routines --default-character-set=utf8mb4 --result-file=service.sql service`.

Un dump fatto con **MySQL 9** si ripristina in modo affidabile solo su MySQL 9: se lo sviluppo usa la 9, nel `.env` imposta `MYSQL_IMAGE=mysql:9.1`
**prima del primo avvio**. La 9.x però è una versione "innovation" con supporto breve; per una produzione che parte da zero resta preferibile la LTS (`mysql:8.4`, predefinita).
Ripristina il dump come al passo 8 e poi avvia. Controllo prima di copiare: `SELECT version, description FROM flyway_schema_history ORDER BY installed_rank;`:
se compaiono migrazioni V3, V4 o V5 con nomi diversi da `appuntamenti_assistenza`, `origine_clienti`, `utenti` (vengono da rami di lavoro non uniti)
il servizio si ferma con "Migration checksum mismatch": non copiare quel database.

## Problemi frequenti

| Sintomo | Causa e rimedio |
|---|---|
| `docker compose up` dice `imposta JWT_SECRET nel file .env` | manca un valore nel `.env` (passo 3) |
| `app` si riavvia in continuazione, nei log `JWT_SECRET` | `JWT_SECRET` sotto i 32 caratteri |
| Nei log `ADMIN_PASSWORD non valida` | meno di 10 caratteri, oppure uguale allo username |
| `Access denied for user 'servizi'` | hai cambiato `DB_PASSWORD` dopo il primo avvio: il database ricorda quella vecchia. Cambiala anche nel database o ricrea il volume (**cancella i dati**) |
| `Migration checksum mismatch` | il database contiene migrazioni diverse da quelle del pacchetto: vedi "Portare i dati" |
| `port is already allocated` | un altro programma usa la porta: cambia `HTTP_PORT` nel `.env` |
| I client non si collegano | rete della VM non in bridge (1.2), firewall (6), oppure la VM è sospesa (1.4) |
| Pagina bianca o versione vecchia dopo un aggiornamento | Ctrl+F5; controlla che `web/` contenga `index.csr.html` |
| Foto o PDF rifiutati | il limite è 20 MB per file; le foto dal tablet vengono ridotte prima dell'invio |
| `permission denied` con `docker` | non sei ancora nel gruppo `docker`: esci e rientra dalla sessione (passo 2) |

## Comandi utili

```bash
docker compose ps                  # stato dei container
docker compose logs -f app         # log dell'applicazione
docker compose restart app         # riavvia solo l'applicazione
docker compose down                # ferma tutto: i DATI RESTANO nel volume
docker compose down -v             # ferma tutto e CANCELLA IL DATABASE: non farlo senza un backup
```

## Cosa è stato verificato (e cosa no)

- **Verificato** su un altro sistema (Docker su Mac, immagini arm64) con **MySQL 8.4 e 9.1 reali**: costruzione dell'immagine, avvio con le 10 migrazioni,
  accesso, sito e API sulla stessa porta, ruolo tecnico (foto, note, chiusura con intervento effettuato, divieti), dati intatti dopo `down` e `up`,
  `backup-db.sh` e ripristino con i comandi di questa guida, applicazione eseguita come utente non privilegiato.
- **Non verificato:** Linux Mint e VirtualBox in sé, l'esecuzione sulle immagini per processori Intel/AMD (sono le stesse immagini ufficiali multi-piattaforma),
  il riavvio automatico dopo `reboot`, `cron` e l'HTTPS del passo 7. Il passo 5 serve proprio a controllarli alla prima installazione.
