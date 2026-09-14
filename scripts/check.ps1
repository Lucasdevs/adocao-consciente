. (Join-Path $PSScriptRoot 'environment.ps1')
Push-Location (Join-Path $projectRoot 'backend')
try {
    & mvn.cmd "-Dmaven.repo.local=$mavenRepository" test
    if ($LASTEXITCODE -ne 0) { throw 'Testes do backend falharam.' }
} finally { Pop-Location }
Push-Location (Join-Path $projectRoot 'frontend')
try {
    & npm.cmd run build
    if ($LASTEXITCODE -ne 0) { throw 'Build do frontend falhou.' }
} finally { Pop-Location }
