$ErrorActionPreference="Stop"
$root=Split-Path -Parent $PSScriptRoot
Push-Location $root
try{$shortRoot=(& cmd.exe /c "for %I in (.) do @echo %~sI").Trim()}finally{Pop-Location}
$localRoot=Join-Path $shortRoot ".mysql-local"; $mysqlRoot=Join-Path $localRoot "mysql"; $pidFile=Join-Path $localRoot "mysql.pid"; $mysqladmin=Join-Path $mysqlRoot "bin\mysqladmin.exe"
if(Test-Path $mysqladmin){& $mysqladmin --host=127.0.0.1 --port=3306 --user=root shutdown 2>$null}
if(Test-Path $pidFile){$mysqlPid=Get-Content $pidFile -ErrorAction SilentlyContinue;if($mysqlPid){Stop-Process -Id $mysqlPid -Force -ErrorAction SilentlyContinue};Remove-Item $pidFile -Force}
Write-Output "MySQL local encerrado."
