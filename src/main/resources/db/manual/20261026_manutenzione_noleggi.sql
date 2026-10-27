CREATE TABLE noleggio_manutenzione_configurazioni (
    doc_id INT NOT NULL,
    soglia_quantita DOUBLE NOT NULL,
    ordinarie_per_straordinaria INT NOT NULL,
    PRIMARY KEY (doc_id),
    CONSTRAINT fk_manutenzione_config_noleggio
        FOREIGN KEY (doc_id) REFERENCES storia_noleggi (doc_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE noleggio_manutenzione_categorie (
    doc_id INT NOT NULL,
    categoria_id INT NOT NULL,
    PRIMARY KEY (doc_id, categoria_id),
    INDEX idx_noleggio_manutenzione_categoria (categoria_id),
    CONSTRAINT fk_manutenzione_categoria_config
        FOREIGN KEY (doc_id) REFERENCES noleggio_manutenzione_configurazioni (doc_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE noleggio_manutenzioni (
    id BIGINT NOT NULL AUTO_INCREMENT,
    doc_id INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    quantita_acquistata_totale DOUBLE NOT NULL,
    eseguita_il DATETIME(6) NOT NULL,
    note TEXT,
    PRIMARY KEY (id),
    INDEX idx_noleggio_manutenzioni_doc_id (doc_id),
    CONSTRAINT fk_manutenzioni_noleggio
        FOREIGN KEY (doc_id) REFERENCES storia_noleggi (doc_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
