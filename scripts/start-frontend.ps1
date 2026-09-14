. (Join-Path $PSScriptRoot 'environment.ps1')
Set-Location (Join-Path $projectRoot 'frontend')
if (-not (Get-Command npm.cmd -ErrorAction SilentlyContinue)) { throw 'Instale Node.js 22.12+ ou use a ferramenta portátil em .tools.' }
if (-not (Test-Path -LiteralPath 'node_modules')) {
    & npm.cmd ci --cache (Join-Path $portableRoot 'npm-cache')
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao instalar dependências do frontend.' }
}
& npm.cmd start
