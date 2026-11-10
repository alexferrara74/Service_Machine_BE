-- Schema iniziale del database `service` (MySQL 9.0).
--
-- Consolida lo stato ottenuto applicando in ordine:
--   integrazione: migrations/001_catalogo_prodotti, 002_classi_sottoclassi, 003_origine_record, 004_rimuovi_noleggi_erp
--   backend:      db/manual/20261026 ... 20261103
-- Su un database vuoto crea tutto; su un database esistente Flyway fa il baseline alla V1
-- senza eseguirla (spring.flyway.baseline-on-migrate). Le modifiche future vanno in V2, V3, ...
-- Le tabelle noleggio_manutenzione_piani/periodi non sono piu' usate dal codice ma restano per non perdere dati.

SET FOREIGN_KEY_CHECKS = 0;
CREATE TABLE `cliente_manutenzione_periodi` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cliente_id` bigint NOT NULL,
  `data_inizio` date NOT NULL,
  `data_fine` date NOT NULL,
  `intervallo_giorni` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cliente_manutenzione_periodo` (`cliente_id`,`data_inizio`,`data_fine`),
  KEY `idx_cliente_manutenzione_periodo_date` (`cliente_id`,`data_inizio`,`data_fine`),
  CONSTRAINT `fk_cliente_manutenzione_periodo_piano` FOREIGN KEY (`cliente_id`) REFERENCES `cliente_manutenzione_piani` (`cliente_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `cliente_manutenzione_piani` (
  `cliente_id` bigint NOT NULL,
  `giorni_avviso` int NOT NULL,
  PRIMARY KEY (`cliente_id`),
  CONSTRAINT `fk_cliente_manutenzione_piano_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `local_clienti` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `interventi` (
  `intervento_id` bigint NOT NULL AUTO_INCREMENT,
  `cliente_id` bigint NOT NULL,
  `matricola_attrezzatura` varchar(100) NOT NULL,
  `titolo` varchar(255) DEFAULT NULL,
  `descrizione_problema` text,
  `soluzione_applicata` text,
  `stato` varchar(50) DEFAULT 'IN_CORSO',
  `data_apertura` datetime DEFAULT CURRENT_TIMESTAMP,
  `data_chiusura` datetime DEFAULT NULL,
  `costo_manodopera` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`intervento_id`),
  KEY `interventi_ibfk_1` (`cliente_id`),
  KEY `interventi_ibfk_2` (`matricola_attrezzatura`),
  CONSTRAINT `interventi_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `local_clienti` (`id`) ON DELETE CASCADE,
  CONSTRAINT `interventi_ibfk_2` FOREIGN KEY (`matricola_attrezzatura`) REFERENCES `local_attrezzature` (`matricola`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `local_attrezzature` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `matricola` varchar(100) DEFAULT NULL,
  `descrizione` varchar(255) DEFAULT NULL,
  `modello` varchar(100) DEFAULT NULL,
  `marca` varchar(100) DEFAULT NULL,
  `stato` varchar(50) DEFAULT 'DISPONIBILE',
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `articolo_id` bigint DEFAULT NULL,
  `origine` varchar(10) NOT NULL DEFAULT 'MANUALE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `matricola` (`matricola`),
  UNIQUE KEY `uq_local_attrezzature_articolo` (`articolo_id`),
  CONSTRAINT `fk_local_attrezzature_prodotto` FOREIGN KEY (`articolo_id`) REFERENCES `local_prodotti` (`articolo_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `local_classi_sottoclassi` (
  `categoria_id` int NOT NULL,
  `classe_codice` char(10) NOT NULL DEFAULT '',
  `sottoclasse_codice` char(10) NOT NULL DEFAULT '',
  `descrizione` char(50) NOT NULL DEFAULT '',
  `tipo` int DEFAULT '0',
  `aggiornato_il` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`categoria_id`),
  UNIQUE KEY `uq_local_classi_sottoclassi_codici` (`classe_codice`,`sottoclasse_codice`),
  KEY `idx_local_classi_sottoclassi_classe` (`classe_codice`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `local_clienti` (
  `id` bigint NOT NULL,
  `codice` varchar(50) DEFAULT NULL,
  `ragione_sociale` varchar(255) DEFAULT NULL,
  `nome` varchar(100) DEFAULT NULL,
  `cognome` varchar(100) DEFAULT NULL,
  `telefono` varchar(50) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `partita_iva` varchar(20) DEFAULT NULL,
  `codice_fiscale` varchar(20) DEFAULT NULL,
  `tipo_listino` int DEFAULT '0',
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `local_prodotti` (
  `articolo_id` bigint NOT NULL,
  `codice` varchar(50) NOT NULL,
  `descrizione` varchar(255) NOT NULL,
  `categoria_id` int DEFAULT NULL,
  `categoria_descrizione` varchar(255) DEFAULT NULL,
  `classe_codice` varchar(10) DEFAULT NULL,
  `sottoclasse_codice` varchar(10) DEFAULT NULL,
  `is_attrezzatura` tinyint(1) NOT NULL DEFAULT '0',
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `origine` varchar(10) NOT NULL DEFAULT 'MANUALE',
  `is_accessorio` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`articolo_id`),
  KEY `idx_local_prodotti_categoria` (`categoria_id`),
  KEY `idx_local_prodotti_attrezzatura` (`is_attrezzatura`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `magazzino_digitale` (
  `ubicazione_id` bigint NOT NULL AUTO_INCREMENT,
  `codice_ubicazione` varchar(50) DEFAULT NULL,
  `zona` varchar(100) DEFAULT NULL,
  `scaffale` varchar(50) DEFAULT NULL,
  `ripiano` varchar(50) DEFAULT NULL,
  `capacita_max` int DEFAULT '0',
  `note` text,
  `creato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ubicazione_id`),
  UNIQUE KEY `codice_ubicazione` (`codice_ubicazione`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `magazzino_posizioni_articoli` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ubicazione_id` bigint NOT NULL,
  `articolo_id` bigint NOT NULL,
  `quantita_presente` int DEFAULT '0',
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_art_pos` (`ubicazione_id`,`articolo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `noleggio_manutenzione_categorie` (
  `doc_id` int NOT NULL,
  `categoria_id` int NOT NULL,
  PRIMARY KEY (`doc_id`,`categoria_id`),
  KEY `idx_noleggio_manutenzione_categoria` (`categoria_id`),
  CONSTRAINT `fk_manutenzione_categoria_config` FOREIGN KEY (`doc_id`) REFERENCES `noleggio_manutenzione_configurazioni` (`doc_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `noleggio_manutenzione_configurazioni` (
  `doc_id` int NOT NULL,
  `soglia_quantita` double NOT NULL,
  `ordinarie_per_straordinaria` int NOT NULL,
  PRIMARY KEY (`doc_id`),
  CONSTRAINT `fk_manutenzione_config_noleggio` FOREIGN KEY (`doc_id`) REFERENCES `storia_noleggi` (`doc_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `noleggio_manutenzione_periodi` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doc_id` int NOT NULL,
  `data_inizio` date NOT NULL,
  `data_fine` date NOT NULL,
  `intervallo_giorni` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_manutenzione_periodo` (`doc_id`,`data_inizio`,`data_fine`),
  KEY `idx_manutenzione_periodo_date` (`doc_id`,`data_inizio`,`data_fine`),
  CONSTRAINT `fk_manutenzione_periodo_piano` FOREIGN KEY (`doc_id`) REFERENCES `noleggio_manutenzione_piani` (`doc_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `noleggio_manutenzione_piani` (
  `doc_id` int NOT NULL,
  `giorni_avviso` int NOT NULL,
  PRIMARY KEY (`doc_id`),
  CONSTRAINT `fk_manutenzione_piano_noleggio` FOREIGN KEY (`doc_id`) REFERENCES `storia_noleggi` (`doc_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `noleggio_manutenzioni` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doc_id` int NOT NULL,
  `tipo` varchar(20) NOT NULL,
  `quantita_acquistata_totale` double NOT NULL,
  `eseguita_il` datetime(6) NOT NULL,
  `note` text,
  PRIMARY KEY (`id`),
  KEY `idx_noleggio_manutenzioni_doc_id` (`doc_id`),
  KEY `idx_noleggio_manutenzioni_doc_id_id` (`doc_id`,`id`),
  CONSTRAINT `fk_manutenzioni_noleggio` FOREIGN KEY (`doc_id`) REFERENCES `storia_noleggi` (`doc_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `ricambistica_interventi` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `intervento_id` bigint NOT NULL,
  `articolo_id` bigint DEFAULT NULL,
  `codice_ricambio` varchar(50) DEFAULT NULL,
  `descrizione_ricambio` varchar(255) DEFAULT NULL,
  `quantita_utilizzata` int DEFAULT '1',
  `prezzo_unitario` double DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `ricambistica_interventi_ibfk_1` (`intervento_id`),
  CONSTRAINT `ricambistica_interventi_ibfk_1` FOREIGN KEY (`intervento_id`) REFERENCES `interventi` (`intervento_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `storia_noleggi` (
  `doc_id` int NOT NULL AUTO_INCREMENT,
  `sezionale` varchar(50) DEFAULT NULL,
  `data_doc` date DEFAULT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `articolo_id` bigint DEFAULT NULL,
  `matricola` varchar(100) DEFAULT NULL,
  `quantita` double(16,3) DEFAULT NULL,
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `erp_anno` smallint unsigned DEFAULT NULL,
  `erp_riga_id` int DEFAULT NULL,
  `codice_articolo` varchar(50) DEFAULT NULL,
  `descrizione` varchar(255) DEFAULT NULL,
  `origine` varchar(10) NOT NULL DEFAULT 'MANUALE',
  PRIMARY KEY (`doc_id`),
  UNIQUE KEY `uq_storia_noleggi_erp_riga` (`erp_anno`,`erp_riga_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `storia_noleggi_documenti` (
  `doc_id` int NOT NULL,
  `nome_file` varchar(255) NOT NULL,
  `content_type` varchar(100) NOT NULL,
  `contenuto` longblob NOT NULL,
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`doc_id`),
  CONSTRAINT `fk_noleggio_documento_noleggio` FOREIGN KEY (`doc_id`) REFERENCES `storia_noleggi` (`doc_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `storia_prodotti` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cliente_id` bigint DEFAULT NULL,
  `codice_cliente` varchar(11) DEFAULT NULL,
  `articolo_id` bigint DEFAULT NULL,
  `codice_articolo` varchar(50) DEFAULT NULL,
  `descrizione` varchar(255) DEFAULT NULL,
  `categoria_id` int DEFAULT NULL,
  `categoria_descrizione` varchar(255) DEFAULT NULL,
  `quantita` double(16,3) DEFAULT NULL,
  `data_doc` date DEFAULT NULL,
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `erp_anno` smallint unsigned DEFAULT NULL,
  `erp_riga_id` int DEFAULT NULL,
  `origine` varchar(10) NOT NULL DEFAULT 'MANUALE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_storia_prodotti_erp_riga` (`erp_anno`,`erp_riga_id`),
  KEY `idx_storia_prodotti_cliente_categoria_quantita` (`cliente_id`,`categoria_id`,`quantita`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
SET FOREIGN_KEY_CHECKS = 1;
