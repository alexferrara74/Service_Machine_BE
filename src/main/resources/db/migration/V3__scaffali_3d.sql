-- Scaffali con misure e posizione in pianta. Ogni cella (ripiano x posizione) è una ubicazione
-- di magazzino_digitale con etichetta generata automaticamente.
CREATE TABLE `magazzino_scaffali` (
  `scaffale_id` bigint NOT NULL AUTO_INCREMENT,
  `codice` varchar(30) NOT NULL,
  `zona` varchar(100) NOT NULL,
  `larghezza_cm` int NOT NULL,
  `profondita_cm` int NOT NULL,
  `altezza_cm` int NOT NULL,
  `pos_x_cm` int NOT NULL DEFAULT '0',
  `pos_y_cm` int NOT NULL DEFAULT '0',
  `rotazione_gradi` int NOT NULL DEFAULT '0',
  `numero_ripiani` int NOT NULL,
  `posizioni_per_ripiano` int NOT NULL,
  `creato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `aggiornato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`scaffale_id`),
  UNIQUE KEY `uq_magazzino_scaffale_codice` (`codice`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `magazzino_digitale`
  ADD COLUMN `scaffale_id` bigint NULL,
  ADD COLUMN `ripiano_num` int NULL,
  ADD COLUMN `posizione_num` int NULL,
  ADD KEY `idx_magazzino_scaffale_id` (`scaffale_id`),
  ADD CONSTRAINT `fk_magazzino_scaffale` FOREIGN KEY (`scaffale_id`) REFERENCES `magazzino_scaffali` (`scaffale_id`);
