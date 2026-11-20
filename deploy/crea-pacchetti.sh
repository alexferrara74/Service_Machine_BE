#!/usr/bin/env bash
# Prepara i pacchetti di installazione (Linux con Docker e Windows Server) da allegare a una release.
#
# Uso:  deploy/crea-pacchetti.sh [cartella-frontend] [cartella-di-uscita]
#   cartella-frontend   dove si trova Service_Machine_FE      (predefinita: ../Service_Machine_FE)
#   cartella-di-uscita  dove scrivere cartelle e zip          (predefinita: ../pacchetti)
#
# Serve: Java 21, Node.js con npm, zip. Costruisce backend e frontend dai sorgenti ATTUALI delle due cartelle:
# prima di una release controlla di essere sul commit giusto in entrambi i repository e che i test passino.
set -euo pipefail

BE="$(cd "$(dirname "$0")/.." && pwd)"
FE="$(cd "${1:-$BE/../Service_Machine_FE}" && pwd)"
mkdir -p "${2:-$BE/../pacchetti}"
OUT="$(cd "${2:-$BE/../pacchetti}" && pwd)"

echo "== Backend (jar)"
(cd "$BE" && bash ./mvnw -q clean package -DskipTests)   # bash: nel repo mvnw non ha il permesso di esecuzione
JAR="$BE/target/demo-0.0.1-SNAPSHOT.jar"

echo "== Frontend (build di produzione)"
(cd "$FE" && npm ci --no-audit --no-fund && npx ng build --configuration production)
WEB="$FE/dist/servizi-pilot-fe/browser"

[ -f "$JAR" ] || { echo "Jar non trovato: $JAR" >&2; exit 1; }
[ -f "$WEB/index.csr.html" ] || { echo "Frontend non trovato (manca index.csr.html): $WEB" >&2; exit 1; }

# Ogni pacchetto si assembla in una cartella di appoggio e poi si sincronizza: nessun file di versioni precedenti resta nel pacchetto.
assembla() {
  local nome="$1" stage; stage="$(mktemp -d)"
  mkdir -p "$stage/app" "$stage/web"
  cp "$JAR" "$stage/app/ServiziPilot.jar"
  cp -R "$WEB/." "$stage/web/"
  case "$nome" in
    Pacchetto_Linux_Docker)
      cp "$BE"/deploy/docker/{Dockerfile,docker-compose.yml,.env.example,.dockerignore,backup-db.sh} "$stage/"
      cp "$BE/deploy/docker/INSTALLAZIONE.md" "$stage/INSTALLAZIONE.md"
      chmod +x "$stage/backup-db.sh" ;;
    Pacchetto_Windows)
      mkdir -p "$stage/servizio"
      cp "$BE"/deploy/windows/{ServiziPilot.xml,backup-db.ps1,crea-database.sql} "$stage/servizio/"
      cp "$BE/deploy/windows/INSTALLAZIONE.md" "$stage/INSTALLAZIONE.md" ;;
  esac
  mkdir -p "$OUT/$nome"
  rsync -a --delete "$stage/" "$OUT/$nome/"
  (cd "$OUT" && zip -qr -FS "$nome.zip" "$nome")
}

assembla Pacchetto_Linux_Docker
assembla Pacchetto_Windows

echo "== Pacchetti in $OUT"
sha() { if command -v sha256sum >/dev/null; then sha256sum "$1"; else shasum -a 256 "$1"; fi | cut -d' ' -f1; }
(cd "$OUT" && for z in Pacchetto_Linux_Docker.zip Pacchetto_Windows.zip; do printf '%s  %s\n' "$(sha "$z")" "$z"; done)
echo "Commit backend:   $(git -C "$BE" rev-parse --short HEAD 2>/dev/null || echo n/d)"
echo "Commit frontend:  $(git -C "$FE" rev-parse --short HEAD 2>/dev/null || echo n/d)"
