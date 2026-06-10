$ErrorActionPreference="Stop"
$root=Split-Path -Parent $PSScriptRoot
$maven=Join-Path $root ".tools\apache-maven-3.9.9\bin\mvn.cmd"
$javaHome="C:\Program Files\Android\Android Studio\jbr"
if(-not(Test-Path $maven)){throw "Maven portatil nao encontrado em .tools."}
if(-not(Test-Path (Join-Path $javaHome "bin\java.exe"))){throw "JDK do Android Studio nao encontrado."}
& (Join-Path $PSScriptRoot "iniciar-mysql-local.ps1")
$env:JAVA_HOME=$javaHome;$env:Path="$javaHome\bin;$env:Path";$env:DB_PRIMARY_HOST="127.0.0.1";$env:DB_REPLICA_HOSTS="127.0.0.1";$env:DB_USER="root";$env:DB_PASSWORD="";$env:DB_NAME="aula-db";$env:DB_PORT="3306";$env:DB_USE_SSL="false";$env:GROUP_NAME="Grupo 8"
try{& $maven spring-boot:run}finally{& (Join-Path $PSScriptRoot "parar-mysql-local.ps1")}
