# Backup del database "service" (dati, foto e PDF compresi: stanno tutti nel DB).
# Uso manuale:   powershell -NoProfile -ExecutionPolicy Bypass -File C:\ServiziPilot\backup-db.ps1
# Pianificato:   vedi INSTALLAZIONE.md, passo 10.
param(
    [string]$MysqlBin    = "C:\Program Files\MySQL\MySQL Server 9.0\bin",   # cartella con mysqldump.exe
    [string]$Destinazione = "C:\ServiziPilot\backup",
    [string]$Credenziali  = "C:\ServiziPilot\config\mysql-backup.cnf",       # [client] user=... password=...
    [int]$GiorniDaTenere  = 30
)
$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Force -Path $Destinazione | Out-Null
$sql = Join-Path $Destinazione ("service_{0:yyyyMMdd_HHmm}.sql" -f (Get-Date))

# --result-file evita la redirezione ">" di PowerShell, che scrive in UTF-16 e rovina il dump
& "$MysqlBin\mysqldump.exe" "--defaults-extra-file=$Credenziali" --single-transaction --routines --no-tablespaces `
    --default-character-set=utf8mb4 "--result-file=$sql" service
if ($LASTEXITCODE -ne 0) { throw "mysqldump fallito (codice $LASTEXITCODE): nessun backup creato." }

Compress-Archive -Path $sql -DestinationPath "$sql.zip"
Remove-Item $sql

# elimina i backup piu' vecchi del periodo da tenere
Get-ChildItem $Destinazione -Filter "service_*.zip" |
    Where-Object LastWriteTime -lt (Get-Date).AddDays(-$GiorniDaTenere) |
    Remove-Item

Write-Host "Backup creato: $sql.zip"
