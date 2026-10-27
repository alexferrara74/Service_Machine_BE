CREATE TABLE noleggio_manutenzione_piani (
    doc_id INT NOT NULL,
    giorni_avviso INT NOT NULL,
    PRIMARY KEY (doc_id),
    CONSTRAINT fk_manutenzione_piano_noleggio
        FOREIGN KEY (doc_id) REFERENCES storia_noleggi (doc_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE noleggio_manutenzione_periodi (
    id BIGINT NOT NULL AUTO_INCREMENT,
    doc_id INT NOT NULL,
    data_inizio DATE NOT NULL,
    data_fine DATE NOT NULL,
    intervallo_giorni INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_manutenzione_periodo (doc_id, data_inizio, data_fine),
    INDEX idx_manutenzione_periodo_date (doc_id, data_inizio, data_fine),
    CONSTRAINT fk_manutenzione_periodo_piano
        FOREIGN KEY (doc_id) REFERENCES noleggio_manutenzione_piani (doc_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
