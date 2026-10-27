CREATE TABLE cliente_manutenzione_piani (
    cliente_id BIGINT NOT NULL,
    giorni_avviso INT NOT NULL,
    PRIMARY KEY (cliente_id),
    CONSTRAINT fk_cliente_manutenzione_piano_cliente
        FOREIGN KEY (cliente_id) REFERENCES local_clienti (id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE cliente_manutenzione_periodi (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    data_inizio DATE NOT NULL,
    data_fine DATE NOT NULL,
    intervallo_giorni INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cliente_manutenzione_periodo (cliente_id, data_inizio, data_fine),
    INDEX idx_cliente_manutenzione_periodo_date (cliente_id, data_inizio, data_fine),
    CONSTRAINT fk_cliente_manutenzione_periodo_piano
        FOREIGN KEY (cliente_id) REFERENCES cliente_manutenzione_piani (cliente_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
