# ThreeEras — instala a pipeline de worldgen no repositorio
param(
    [string]$RepoRoot = "",
    [switch]$DryRun,
    [switch]$Force,
    [switch]$WithConfig
)

$ErrorActionPreference = "Stop"
$Source = $PSScriptRoot

function Write-Step($n, $msg) {
    Write-Host ""
    Write-Host ("=" * 68) -ForegroundColor DarkGray
    Write-Host "  [$n] $msg" -ForegroundColor Cyan
    Write-Host ("=" * 68) -ForegroundColor DarkGray
}
function Write-Ok($m)   { Write-Host "  OK  $m" -ForegroundColor Green }
function Write-Warn($m) { Write-Host "  !   $m" -ForegroundColor Yellow }
function Write-Err($m)  { Write-Host "  X   $m" -ForegroundColor Red }
function Write-Info($m) { Write-Host "      $m" -ForegroundColor DarkGray }

Write-Step 0 "Localizando o repositorio Tr-sEras"

if (-not $RepoRoot) {
    $candidates = @(
        (Join-Path $env:USERPROFILE "Documents\Tr-sEras"),
        (Join-Path $env:USERPROFILE "Documents\Três Eras"),
        (Join-Path $env:USERPROFILE "Documents\ThreeEras"),
        (Join-Path $env:USERPROFILE "Tr-sEras")
    )
    foreach ($c in $candidates) {
        if (Test-Path (Join-Path $c "ThreeEras\pom.xml")) { $RepoRoot = $c; break }
    }
}

if (-not $RepoRoot -or -not (Test-Path $RepoRoot)) {
    Write-Err "Repositorio nao encontrado."
    exit 1
}

$pom = Join-Path $RepoRoot "ThreeEras\pom.xml"
if (-not (Test-Path $pom)) {
    Write-Warn "Nao encontrei ThreeEras\pom.xml em $RepoRoot"
    if (-not $Force) {
        $ans = Read-Host "  Continuar mesmo assim? (s/N)"
        if ($ans -notmatch '^[sS]') { exit 1 }
    }
}
Write-Ok "repositorio: $RepoRoot"

Write-Step 1 "Planejando a copia"

$plan = @()

$srcPipeline = Join-Path $Source "pipeline"
if (Test-Path $srcPipeline) {
    Get-ChildItem -Path $srcPipeline -Filter "*.py" -File | ForEach-Object {
        $plan += @{ From = $_.FullName; To = Join-Path $RepoRoot "pipeline\$($_.Name)" }
    }
} else {
    Write-Err "pasta pipeline/ nao encontrada em $Source"
    exit 1
}

foreach ($f in @("requirements.txt", "setup_env.ps1", "setup_env.sh")) {
    $src = Join-Path $Source $f
    if (Test-Path $src) { $plan += @{ From = $src; To = Join-Path $RepoRoot $f } }
}

if ($WithConfig) {
    $srcCfg = Join-Path $Source "config"
    if (Test-Path $srcCfg) {
        Get-ChildItem -Path $srcCfg -Filter "*.json" -File | ForEach-Object {
            $plan += @{ From = $_.FullName; To = Join-Path $RepoRoot "config\$($_.Name)" }
        }
    }
}

Write-Info "$($plan.Count) arquivo(s) a copiar"
foreach ($p in $plan) {
    $rel = $p.To.Substring($RepoRoot.Length).TrimStart('\')
    $mark = if (Test-Path $p.To) { "(sobrescreve)" } else { "(novo)" }
    Write-Info "  $rel $mark"
}

if ($DryRun) {
    Write-Warn "-DryRun: nada foi copiado."
    exit 0
}

Write-Step 2 "Copiando arquivos"

$overwrites = $plan | Where-Object { Test-Path $_.To }
if ($overwrites.Count -gt 0 -and -not $Force) {
    Write-Warn "$($overwrites.Count) arquivo(s) serao sobrescritos."
    $ans = Read-Host "  Continuar? (s/N)"
    if ($ans -notmatch '^[sS]') { Write-Warn "Cancelado."; exit 0 }
}

$copied = 0
foreach ($p in $plan) {
    $dir = Split-Path -Parent $p.To
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    Copy-Item -Path $p.From -Destination $p.To -Force
    $copied++
}
Write-Ok "$copied arquivo(s) copiados"

Write-Step 3 "Atualizando .gitignore"

$gi = Join-Path $RepoRoot ".gitignore"
$block = @(
    "",
    "# --- Pipeline de worldgen (Python) ---",
    ".venv/",
    "venv/",
    "__pycache__/",
    "*.pyc",
    "*.pyo",
    ".pytest_cache/",
    ".mypy_cache/",
    "",
    "# artefatos de geracao (heightmap, biomemap, blockmap)",
    "artifacts/",
    "",
    "# mundos gerados pela pipeline",
    "world_threeeras_medieval/",
    ""
)

$existing = if (Test-Path $gi) { Get-Content $gi -Raw } else { "" }
if ($existing -notmatch "Pipeline de worldgen") {
    Add-Content -Path $gi -Value ($block -join "`n")
    Write-Ok ".gitignore atualizado"
} else {
    Write-Warn ".gitignore ja contem o bloco da pipeline — nada a fazer"
}

Write-Step 4 "Verificando"

$checks = @(
    @{ Path = "pipeline\schema.py";           Label = "S0 schema" },
    @{ Path = "pipeline\noise.py";            Label = "S1 noise" },
    @{ Path = "pipeline\influence.py";        Label = "S1 influence" },
    @{ Path = "pipeline\biome.py";            Label = "S2 biome" },
    @{ Path = "pipeline\surface.py";          Label = "S3 surface" },
    @{ Path = "pipeline\smoke_test_amulet.py"; Label = "backend Anvil" },
    @{ Path = "requirements.txt";             Label = "dependencias" },
    @{ Path = "setup_env.ps1";                Label = "setup do ambiente" }
)
$missing = 0
foreach ($c in $checks) {
    $full = Join-Path $RepoRoot $c.Path
    if (Test-Path $full) { Write-Ok "$($c.Label): $($c.Path)" }
    else { Write-Err "FALTANDO: $($c.Path)"; $missing++ }
}

if ($WithConfig) {
    foreach ($f in @("worldplan_medieval.json", "palette_medieval.json")) {
        $full = Join-Path $RepoRoot "config\$f"
        if (Test-Path $full) { Write-Ok "config: $f" } else { Write-Err "FALTANDO: config\$f"; $missing++ }
    }
}

Write-Host ""
if ($missing -eq 0) {
    Write-Host ("=" * 68) -ForegroundColor DarkGray
    Write-Host "  INSTALACAO CONCLUIDA" -ForegroundColor Green
    Write-Host ("=" * 68) -ForegroundColor DarkGray
} else {
    Write-Host ("=" * 68) -ForegroundColor DarkGray
    Write-Host "  CONCLUIDA COM $missing PENDENCIA(S)" -ForegroundColor Yellow
    Write-Host ("=" * 68) -ForegroundColor DarkGray
}
