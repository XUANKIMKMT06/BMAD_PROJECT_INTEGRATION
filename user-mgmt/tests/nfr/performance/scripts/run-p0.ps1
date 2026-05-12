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

$scripts = @(
    'tests/nfr/performance/pf01-admin-list.k6.js',
    'tests/nfr/performance/pf02-login-storm.k6.js',
    'tests/nfr/performance/pf03-home.k6.js',
    'tests/nfr/performance/pf04-mixed-admin.k6.js'
)

Push-Location $moduleRoot
try {
    $env:BASE_URL = $BaseUrl
    $env:PERF_ARTIFACT_DIR = $artifactDir

    foreach ($script in $scripts) {
        Write-Host "Running $script"
        k6 run $script
        if ($LASTEXITCODE -ne 0) {
            throw "k6 failed for $script (exit $LASTEXITCODE)"
        }
    }
}
finally {
    Pop-Location
}
