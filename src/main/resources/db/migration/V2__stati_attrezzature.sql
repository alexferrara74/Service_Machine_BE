-- Allinea gli stati delle attrezzature ai valori documentati (DISPONIBILE, IN_USO, IN_MANUTENZIONE).
-- Il form FE salvava "Noleggiata" e "In Manutenzione": il filtro per stato e l'ERP non li riconoscevano.
UPDATE local_attrezzature SET stato = 'IN_USO' WHERE LOWER(TRIM(stato)) IN ('noleggiata', 'in uso');
UPDATE local_attrezzature SET stato = 'IN_MANUTENZIONE' WHERE LOWER(REPLACE(TRIM(stato), ' ', '_')) = 'in_manutenzione';
UPDATE local_attrezzature SET stato = 'DISPONIBILE' WHERE LOWER(TRIM(stato)) = 'disponibile';
