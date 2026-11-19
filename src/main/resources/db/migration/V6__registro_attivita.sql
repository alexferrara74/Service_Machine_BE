-- Registro delle attività: accessi e operazioni di scrittura degli utenti, solo in aggiunta (nessun update/delete da API).
-- Non contiene i corpi delle richieste né le password.
CREATE TABLE `registro_attivita` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quando` datetime(3) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `ruolo` varchar(20) DEFAULT NULL,
  `azione` varchar(30) NOT NULL,
  `risorsa` varchar(80) DEFAULT NULL,
  `risorsa_id` varchar(40) DEFAULT NULL,
  `esito` varchar(15) NOT NULL,
  `codice_http` int DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `dettaglio` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_registro_quando` (`quando`),
  KEY `idx_registro_username` (`username`),
  KEY `idx_registro_azione` (`azione`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
