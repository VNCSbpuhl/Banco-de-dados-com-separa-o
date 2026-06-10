$ErrorActionPreference = "Stop"
$root=Split-Path -Parent $PSScriptRoot
Push-Location $root
try{$shortRoot=(& cmd.exe /c "for %I in (.) do @echo %~sI").Trim()}finally{Pop-Location}
$localRoot=Join-Path $shortRoot ".mysql-local"; $mysqlRoot=Join-Path $localRoot "mysql"; $logDir=Join-Path $localRoot "logs"; $pidFile=Join-Path $localRoot "mysql.pid"; $configFile=Join-Path $localRoot "my.ini"; $mysqld=Join-Path $mysqlRoot "bin\mysqld.exe"; $mysqladmin=Join-Path $mysqlRoot "bin\mysqladmin.exe"
if(-not(Test-Path $mysqld)){throw "Execute instalar-mysql-local.ps1 primeiro."}
New-Item -ItemType Directory -Force $logDir|Out-Null
& $mysqladmin --host=127.0.0.1 --port=3306 --user=root ping --silent 2>$null
if($LASTEXITCODE -eq 0){Write-Output "MySQL local ja esta em execucao.";exit 0}
if(Test-Path $pidFile){Remove-Item $pidFile -Force}
& cmd.exe /c "start `"`" /b `"$mysqld`" --defaults-file=`"$configFile`""
Start-Sleep -Milliseconds 500
$process=Get-Process mysqld -ErrorAction SilentlyContinue|Sort-Object StartTime -Descending|Select-Object -First 1
if(-not $process){throw "O processo mysqld nao foi iniciado."}
Set-Content $pidFile $process.Id
for($attempt=1;$attempt -le 30;$attempt++){& $mysqladmin --host=127.0.0.1 --port=3306 --user=root ping --silent 2>$null;if($LASTEXITCODE -eq 0){Write-Output "MySQL local iniciado em 127.0.0.1:3306.";exit 0};Start-Sleep -Milliseconds 500}
throw "MySQL nao respondeu."
