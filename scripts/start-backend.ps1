param([switch]$Postgres, [switch]$WithoutDemo)
. (Join-Path $PSScriptRoot 'environment.ps1')
Set-Location (Join-Path $projectRoot 'backend')
if (-not (Get-Command mvn.cmd -ErrorAction SilentlyContinue)) { throw 'Instale Java 21 e Maven 3.9 ou use as ferramentas portáteis em .tools.' }
$profiles = if ($Postgres) { '' } else { 'local' }
if (-not $WithoutDemo -and -not $Postgres) { $profiles = 'local,demo' }
$arguments = @("-Dmaven.repo.local=$mavenRepository", 'spring-boot:run')
if ($profiles) { $arguments += "-Dspring-boot.run.profiles=$profiles" }
& mvn.cmd @arguments
if ($LASTEXITCODE -ne 0) { throw 'O backend não iniciou. Consulte o erro acima.' }
