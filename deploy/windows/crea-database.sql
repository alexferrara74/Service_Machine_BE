-- Crea il database e l'utente dell'applicazione. Eseguilo UNA volta come root di MySQL (INSTALLAZIONE.md, passo 3).
-- Sostituisci SOSTITUISCI-PASSWORD con la stessa password che metti in DB_PASSWORD dentro ServiziPilot.xml.
-- Le tabelle le crea l'applicazione al primo avvio (Flyway): qui non serve altro.

CREATE DATABASE IF NOT EXISTS service
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'servizi'@'localhost' IDENTIFIED BY 'SOSTITUISCI-PASSWORD';

-- Servono anche i permessi di struttura (CREATE/ALTER): le migrazioni modificano le tabelle.
GRANT ALL PRIVILEGES ON service.* TO 'servizi'@'localhost';
FLUSH PRIVILEGES;
