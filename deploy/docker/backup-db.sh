#!/usr/bin/env bash
# Backup del database (dati, foto e PDF compresi: stanno tutti nel database).
# Uso:  ./backup-db.sh            (da qualunque cartella: si sposta da solo in quella dello script)
# Pianificato ogni notte: vedi INSTALLAZIONE.md, passo 8.
set -euo pipefail
cd "$(dirname "$0")"

GIORNI_DA_TENERE="${GIORNI_DA_TENERE:-30}"
DEST="${DEST:-./backup}"
mkdir -p "$DEST"

# la password viene letta dal file .env e passata al container senza comparire nei processi del server
set -a; . ./.env; set +a
FILE="$DEST/service_$(date +%Y%m%d_%H%M).sql.gz"

docker compose exec -T -e MYSQL_PWD="$DB_PASSWORD" db \
  mysqldump -u servizi --single-transaction --routines --no-tablespaces --default-character-set=utf8mb4 service \
  | gzip > "$FILE.parziale"

# un dump troppo piccolo è quasi sempre un errore (database fermo, password sbagliata): non lo si tiene
if [ "$(stat -c %s "$FILE.parziale" 2>/dev/null || stat -f %z "$FILE.parziale")" -lt 2000 ]; then
  rm -f "$FILE.parziale"
  echo "Backup fallito: il dump è vuoto o troppo piccolo." >&2
  exit 1
fi
mv "$FILE.parziale" "$FILE"

# elimina i backup più vecchi del periodo da tenere
find "$DEST" -name 'service_*.sql.gz' -mtime +"$GIORNI_DA_TENERE" -delete
echo "Backup creato: $FILE"
