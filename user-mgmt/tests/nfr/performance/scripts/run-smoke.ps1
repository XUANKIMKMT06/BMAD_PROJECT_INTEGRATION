param(
    [string]$BaseUrl = $env:BASE_URL
)

$ErrorActionPreference = 'Stop'

if (-not $BaseUrl) {
    $BaseUrl = 'http://localhost:8080'
}

$moduleRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $PSScriptRoot)))
$repoRoot = Split-Path -Parent $moduleRoot
$artifactDir = Join-Path $repoRoot '_bmad-output\test-artifacts\performance'

Push-Location $moduleRoot
try {
    $env:BASE_URL = $BaseUrl
    $env:PERF_ARTIFACT_DIR = $artifactDir
    k6 run tests/nfr/performance/smoke.k6.js
}
finally {
    Pop-Location
}
