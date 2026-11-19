-- Distingue i clienti importati dall'ERP da quelli creati a mano dall'operatore (come già avviene per
-- local_prodotti, local_attrezzature e storia_noleggi). Le righe esistenti e quelle che scrive il
-- microservizio ERP (che non conosce la colonna) restano 'ERP'; il backend imposta 'MANUALE' alla creazione.
ALTER TABLE `local_clienti`
  ADD COLUMN `origine` varchar(10) NOT NULL DEFAULT 'ERP';
