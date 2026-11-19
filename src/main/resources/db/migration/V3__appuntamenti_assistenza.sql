-- Calendario assistenze. Un appuntamento riguarda un noleggio censito (doc_id valorizzato:
-- cliente e matricola si leggono dal noleggio) oppure, in via eccezionale, un cliente non
-- censito inserito a mano (doc_id NULL, cliente_nome obbligatorio).
CREATE TABLE `appuntamenti_assistenza` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doc_id` int DEFAULT NULL,
  `cliente_nome` varchar(255) DEFAULT NULL,
  `cliente_telefono` varchar(50) DEFAULT NULL,
  `indirizzo` varchar(255) DEFAULT NULL,
  `apparecchio` varchar(255) DEFAULT NULL,
  `titolo` varchar(255) NOT NULL,
  `note` text,
  `stato` varchar(20) NOT NULL DEFAULT 'DA_CONFERMARE',
  `inizio` datetime NOT NULL,
  `fine` datetime NOT NULL,
  `tecnico` varchar(100) DEFAULT NULL,
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_appuntamenti_inizio` (`inizio`),
  KEY `idx_appuntamenti_stato` (`stato`),
  KEY `idx_appuntamenti_doc_id` (`doc_id`),
  CONSTRAINT `fk_appuntamenti_noleggio` FOREIGN KEY (`doc_id`) REFERENCES `storia_noleggi` (`doc_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
