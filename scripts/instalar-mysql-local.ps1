$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Push-Location $root
try { $shortRoot = (& cmd.exe /c "for %I in (.) do @echo %~sI").Trim() } finally { Pop-Location }
$localRoot = Join-Path $shortRoot ".mysql-local"
$archive = Join-Path $localRoot "mysql-8.4.9-winx64.zip"
$extracted = Join-Path $localRoot "mysql-8.4.9-winx64"
$mysqlRoot = Join-Path $localRoot "mysql"
$dataDir = Join-Path $localRoot "data"
$configFile = Join-Path $localRoot "my.ini"
New-Item -ItemType Directory -Force $localRoot | Out-Null
if (-not (Test-Path $archive)) { Invoke-WebRequest "https://cdn.mysql.com/Downloads/MySQL-8.4/mysql-8.4.9-winx64.zip" -OutFile $archive }
if (-not (Test-Path $mysqlRoot)) { Expand-Archive -LiteralPath $archive -DestinationPath $localRoot -Force; Move-Item -LiteralPath $extracted -Destination $mysqlRoot }
$basedir=$mysqlRoot.Replace("\","/"); $datadir=$dataDir.Replace("\","/")
@"
[mysqld]
basedir=$basedir
datadir=$datadir
port=3306
bind-address=127.0.0.1
mysqlx=0
log-error=$($localRoot.Replace("\","/"))/logs/mysql.err.log
character-set-server=utf8mb4
collation-server=utf8mb4_0900_ai_ci
[client]
host=127.0.0.1
port=3306
user=root
default-character-set=utf8mb4
"@ | Set-Content $configFile -Encoding ASCII
if (-not (Test-Path (Join-Path $dataDir "mysql"))) { New-Item -ItemType Directory -Force $dataDir | Out-Null; & (Join-Path $mysqlRoot "bin\mysqld.exe") "--defaults-file=$configFile" --initialize-insecure; if($LASTEXITCODE -ne 0){throw "Falha ao inicializar MySQL"} }
& (Join-Path $PSScriptRoot "iniciar-mysql-local.ps1")
Get-Content -Raw (Join-Path $root "database\schema.sql") | & (Join-Path $mysqlRoot "bin\mysql.exe") --host=127.0.0.1 --port=3306 --user=root
Write-Output "MySQL local instalado e banco aula-db criado."
