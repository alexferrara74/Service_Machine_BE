CREATE TABLE IF NOT EXISTS storia_noleggi_documenti (
    doc_id INT NOT NULL,
    nome_file VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    contenuto LONGBLOB NOT NULL,
    aggiornato_il TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (doc_id),
    CONSTRAINT fk_noleggio_documento_noleggio
        FOREIGN KEY (doc_id)
        REFERENCES storia_noleggi (doc_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
