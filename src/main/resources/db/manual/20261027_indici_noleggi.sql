CREATE INDEX idx_storia_prodotti_cliente_categoria_quantita
    ON storia_prodotti (cliente_id, categoria_id, quantita);

CREATE INDEX idx_noleggio_manutenzioni_doc_id_id
    ON noleggio_manutenzioni (doc_id, id);
