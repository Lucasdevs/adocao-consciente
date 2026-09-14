$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$portableRoot = Join-Path $projectRoot '.tools'
if (Test-Path -LiteralPath $portableRoot) {
    $javaTool = Get-ChildItem -LiteralPath $portableRoot -Directory -Filter 'jdk-*' | Select-Object -First 1
    $nodeTool = Get-ChildItem -LiteralPath $portableRoot -Directory -Filter 'node-*-win-x64' | Select-Object -First 1
    $mavenTool = Get-ChildItem -LiteralPath $portableRoot -Directory -Filter 'apache-maven-*' | Select-Object -First 1
    if ($javaTool) { $env:JAVA_HOME = $javaTool.FullName; $env:PATH = (Join-Path $javaTool.FullName 'bin') + ';' + $env:PATH }
    if ($nodeTool) { $env:PATH = $nodeTool.FullName + ';' + $env:PATH }
    if ($mavenTool) { $env:PATH = (Join-Path $mavenTool.FullName 'bin') + ';' + $env:PATH }
}
$mavenRepository = Join-Path $portableRoot 'm2'
