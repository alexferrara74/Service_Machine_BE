-- Appuntamenti del calendario: foto scattate dal tecnico con il tablet e collegamento all'intervento
-- effettuato che nasce quando l'appuntamento viene chiuso.
ALTER TABLE `appuntamenti_assistenza` ADD COLUMN `intervento_id` bigint NULL;

-- Le foto stanno nel database come quelle degli appuntamenti di manutenzione: un solo backup copre tutto.
CREATE TABLE `appuntamenti_assistenza_foto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `appuntamento_id` bigint NOT NULL,
  `nome_file` varchar(255) NOT NULL,
  `content_type` varchar(100) NOT NULL,
  `contenuto` longblob NOT NULL,
  `creato_il` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_assistenza_foto_appuntamento` (`appuntamento_id`),
  CONSTRAINT `fk_assistenza_foto_appuntamento` FOREIGN KEY (`appuntamento_id`) REFERENCES `appuntamenti_assistenza` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
