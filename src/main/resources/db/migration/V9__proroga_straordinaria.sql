-- La proroga con esito positivo può riguardare la manutenzione ordinaria e/o la straordinaria.
ALTER TABLE `noleggio_appuntamenti` ADD COLUMN `giorni_proroga_straordinaria` int NULL;
ALTER TABLE `noleggio_manutenzioni` ADD COLUMN `giorni_proroga_straordinaria` int NULL;
