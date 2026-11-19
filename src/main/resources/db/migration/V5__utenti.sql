-- Utenti dell'applicazione. Ruoli: ADMIN (può tutto) e VIEWER (sola lettura).
-- La password è salvata solo come hash BCrypt. Il primo ADMIN viene creato all'avvio dal backend
-- (variabili ADMIN_USERNAME / ADMIN_PASSWORD) quando la tabella è vuota.
CREATE TABLE `utenti` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(100) NOT NULL,
  `ruolo` varchar(20) NOT NULL,
  `attivo` tinyint(1) NOT NULL DEFAULT '1',
  `password_modificata_il` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ultimo_accesso` datetime DEFAULT NULL,
  `creato_il` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_utenti_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
