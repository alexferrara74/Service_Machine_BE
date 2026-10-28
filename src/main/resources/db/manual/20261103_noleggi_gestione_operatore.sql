-- I noleggi sono inseriti dall'operatore: doc_id viene generato dal database
-- e resta la chiave del PDF in storia_noleggi_documenti.
-- MySQL non permette di modificare una colonna referenziata da una FK (errore 1833):
-- si rimuovono le FK verso storia_noleggi, si rende doc_id AUTO_INCREMENT e si
-- ricreano le FK identiche. Eseguire dopo un backup e senza traffico sul servizio.
--
-- Verifica preventiva dei nomi reali delle FK:
--   SELECT TABLE_NAME, CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS
--   WHERE CONSTRAINT_SCHEMA = 'service' AND REFERENCED_TABLE_NAME = 'storia_noleggi';

ALTER TABLE storia_noleggi_documenti          DROP FOREIGN KEY fk_noleggio_documento_noleggio;
ALTER TABLE noleggio_manutenzione_piani       DROP FOREIGN KEY fk_manutenzione_piano_noleggio;
ALTER TABLE noleggio_manutenzione_configurazioni DROP FOREIGN KEY fk_manutenzione_config_noleggio;
ALTER TABLE noleggio_manutenzioni             DROP FOREIGN KEY fk_manutenzioni_noleggio;

ALTER TABLE storia_noleggi
    MODIFY COLUMN doc_id INT NOT NULL AUTO_INCREMENT;

ALTER TABLE storia_noleggi_documenti
    ADD CONSTRAINT fk_noleggio_documento_noleggio
        FOREIGN KEY (doc_id) REFERENCES storia_noleggi (doc_id) ON DELETE CASCADE;
ALTER TABLE noleggio_manutenzione_piani
    ADD CONSTRAINT fk_manutenzione_piano_noleggio
        FOREIGN KEY (doc_id) REFERENCES storia_noleggi (doc_id) ON DELETE CASCADE;
ALTER TABLE noleggio_manutenzione_configurazioni
    ADD CONSTRAINT fk_manutenzione_config_noleggio
        FOREIGN KEY (doc_id) REFERENCES storia_noleggi (doc_id) ON DELETE CASCADE;
ALTER TABLE noleggio_manutenzioni
    ADD CONSTRAINT fk_manutenzioni_noleggio
        FOREIGN KEY (doc_id) REFERENCES storia_noleggi (doc_id) ON DELETE CASCADE;
