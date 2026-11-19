-- Appuntamenti di manutenzione sui noleggi: resoconto, foto, esito e proroga della scadenza.
CREATE TABLE `noleggio_appuntamenti` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doc_id` int NOT NULL,
  `data_prevista` datetime(6) NOT NULL,
  `stato` varchar(20) NOT NULL,
  `tipo` varchar(20) NOT NULL,
  `esito` varchar(20) DEFAULT NULL,
  `resoconto` text,
  `giorni_proroga` int DEFAULT NULL,
  `completato_il` datetime(6) DEFAULT NULL,
  `manutenzione_id` bigint DEFAULT NULL,
  `creato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_appuntamenti_doc_id` (`doc_id`),
  KEY `idx_appuntamenti_stato_data` (`stato`, `data_prevista`),
  CONSTRAINT `fk_appuntamento_noleggio` FOREIGN KEY (`doc_id`) REFERENCES `storia_noleggi` (`doc_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `noleggio_appuntamento_foto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `appuntamento_id` bigint NOT NULL,
  `nome_file` varchar(255) NOT NULL,
  `content_type` varchar(100) NOT NULL,
  `contenuto` longblob NOT NULL,
  `creato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_appuntamento_foto_appuntamento` (`appuntamento_id`),
  CONSTRAINT `fk_foto_appuntamento` FOREIGN KEY (`appuntamento_id`) REFERENCES `noleggio_appuntamenti` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `noleggio_manutenzioni`
  ADD COLUMN `esito` varchar(20) NULL,
  ADD COLUMN `giorni_proroga` int NULL,
  ADD COLUMN `appuntamento_id` bigint NULL;
