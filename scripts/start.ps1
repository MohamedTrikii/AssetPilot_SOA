# =====================================================================
#  AssetPilot - script de lancement (Windows PowerShell 5.1+)
#
#  Usage :
#    .\scripts\start.ps1                 -> menu interactif
#    .\scripts\start.ps1 -Mode docker    -> docker compose up --build
#    .\scripts\start.ps1 -Mode local     -> 4 services avec Maven (une fenetre chacun)
#    .\scripts\start.ps1 -Mode local -NoBrowser
# =====================================================================
param(
    [ValidateSet('docker', 'local', '')]
    [string]$Mode = '',
    [switch]$NoBrowser
)

# 'Continue' : en PowerShell 5.1, la sortie stderr des commandes natives (docker, mvn)
# deviendrait sinon une erreur fatale ; on teste $LASTEXITCODE a la place.
$ErrorActionPreference = 'Continue'
$Root = Split-Path -Parent $PSScriptRoot

# Services dans l'ordre de demarrage : Warranty + Maintenance -> Asset -> Gateway
$Services = @(
    @{ Name = 'warranty-service';    Port = 8082; Health = 'http://localhost:8082/actuator/health' },
    @{ Name = 'maintenance-service'; Port = 8084; Health = 'http://localhost:8084/actuator/health' },
    @{ Name = 'asset-service';       Port = 8081; Health = 'http://localhost:8081/actuator/health' },
    @{ Name = 'gateway';             Port = 8083; Health = 'http://localhost:8083/actuator/health' }
)
$AllPorts = @(8081, 8082, 8083, 8084, 9090)

function Write-Step($msg)  { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg)    { Write-Host "    [OK] $msg" -ForegroundColor Green }
function Write-Warn2($msg) { Write-Host "    [!]  $msg" -ForegroundColor Yellow }
function Write-Err($msg)   { Write-Host "    [X]  $msg" -ForegroundColor Red }

function Test-Command($name) {
    return $null -ne (Get-Command $name -ErrorAction SilentlyContinue)
}

function Test-Healthy($url) {
    try {
        $r = Invoke-RestMethod -Uri $url -TimeoutSec 3
        return $r.status -eq 'UP'
    } catch {
        return $false
    }
}

function Wait-Healthy($service, $timeoutSec) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    Write-Host "    attente de $($service.Name) ($($service.Health))" -NoNewline
    while ((Get-Date) -lt $deadline) {
        if (Test-Healthy $service.Health) {
            Write-Host ''
            Write-Ok "$($service.Name) est UP"
            return $true
        }
        Write-Host '.' -NoNewline
        Start-Sleep -Seconds 3
    }
    Write-Host ''
    Write-Err "$($service.Name) n'a pas demarre en $timeoutSec s (voir sa fenetre / ses logs)"
    return $false
}

function Get-BusyPorts {
    $busy = @()
    foreach ($p in $AllPorts) {
        $c = Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue
        if ($c) { $busy += $p }
    }
    return $busy
}

function Show-Summary {
    Write-Host ''
    Write-Host '=====================================================================' -ForegroundColor Green
    Write-Host '  AssetPilot est demarre' -ForegroundColor Green
    Write-Host '=====================================================================' -ForegroundColor Green
    Write-Host '  API Gateway (front-end)  http://localhost:8083'
    Write-Host '  Swagger UI               http://localhost:8083/swagger-ui.html'
    Write-Host '  OpenAPI JSON             http://localhost:8083/v3/api-docs'
    Write-Host '  WSDL (SOAP)              http://localhost:8083/ws/warranties.wsdl'
    Write-Host '  Asset Service (direct)   http://localhost:8081'
    Write-Host '  Warranty Service         http://localhost:8082/ws'
    Write-Host '  Maintenance gRPC         localhost:9090   (health : http://localhost:8084/actuator/health)'
    Write-Host ''
    Write-Host '  Postman : importer postman\AssetPilot.postman_collection.json'
    Write-Host '  Arret   : scripts\stop.bat  (ou .\scripts\stop.ps1)'
    Write-Host '=====================================================================' -ForegroundColor Green
    if (-not $NoBrowser) {
        Start-Process 'http://localhost:8083/swagger-ui.html'
    }
}

# ---------------------------------------------------------------- choix du mode
if ($Mode -eq '') {
    Write-Host ''
    Write-Host '  AssetPilot - lancement' -ForegroundColor Cyan
    Write-Host '  [1] Docker Compose (recommande)'
    Write-Host '  [2] Local avec Maven (4 fenetres)'
    $choice = Read-Host '  Votre choix (1/2)'
    if ($choice -eq '2') { $Mode = 'local' } else { $Mode = 'docker' }
}

$busy = Get-BusyPorts
if ($busy.Count -gt 0) {
    Write-Warn2 "Ports deja utilises : $($busy -join ', ')"
    Write-Warn2 'AssetPilot tourne peut-etre deja. Lancez scripts\stop.bat pour tout arreter.'
    $answer = Read-Host '    Continuer quand meme ? (o/N)'
    if ($answer -ne 'o' -and $answer -ne 'O') { exit 1 }
}

# ---------------------------------------------------------------- mode Docker
if ($Mode -eq 'docker') {
    Write-Step 'Verification de Docker'
    if (-not (Test-Command 'docker')) {
        Write-Err 'Docker n est pas installe. Utilisez le mode local : .\scripts\start.ps1 -Mode local'
        exit 1
    }
    docker info 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Err 'Le moteur Docker ne repond pas : demarrez Docker Desktop puis relancez.'
        exit 1
    }
    Write-Ok 'Docker est disponible'

    Write-Step 'Construction et demarrage des conteneurs (premiere fois : plusieurs minutes)'
    Push-Location $Root
    try {
        docker compose up --build -d
        if ($LASTEXITCODE -ne 0) { Write-Err 'docker compose a echoue'; exit 1 }
    } finally {
        Pop-Location
    }

    Write-Step 'Attente des healthchecks'
    foreach ($s in $Services) {
        if (-not (Wait-Healthy $s 300)) {
            Write-Warn2 'Logs : docker compose logs -f'
            exit 1
        }
    }
    Show-Summary
    exit 0
}

# ---------------------------------------------------------------- mode local
Write-Step 'Verification de Java et Maven'
if (-not (Test-Command 'java')) { Write-Err 'Java introuvable (JDK 17+ requis)'; exit 1 }
if (-not (Test-Command 'mvn'))  { Write-Err 'Maven introuvable (mvn doit etre dans le PATH)'; exit 1 }
Write-Ok 'Java et Maven disponibles'

function Start-ServiceWindow($service) {
    $dir = Join-Path $Root $service.Name
    $title = "AssetPilot - $($service.Name)"
    # Jusqu'a 3 tentatives : un IDE ouvert (VS Code / IntelliJ) peut regenerer target/ en meme temps
    # que Maven et provoquer une erreur de compilation passagere ("error reading ...").
    $cmd = "`$Host.UI.RawUI.WindowTitle = '$title'; Set-Location '$dir'; " +
           "for (`$i = 1; `$i -le 3; `$i++) { mvn -B spring-boot:run; if (`$LASTEXITCODE -eq 0) { break }; " +
           "Write-Host ('Echec du demarrage (tentative ' + `$i + '/3), nouvelle tentative...') -ForegroundColor Yellow; Start-Sleep -Seconds 5 }"
    Start-Process powershell -ArgumentList '-NoExit', '-Command', $cmd | Out-Null
    Write-Ok "$($service.Name) lance dans une nouvelle fenetre"
}

Write-Step 'Demarrage de Warranty et Maintenance (en parallele)'
Start-ServiceWindow $Services[0]
Start-ServiceWindow $Services[1]
if (-not (Wait-Healthy $Services[0] 300)) { exit 1 }
if (-not (Wait-Healthy $Services[1] 300)) { exit 1 }

Write-Step 'Demarrage de Asset Service'
Start-ServiceWindow $Services[2]
if (-not (Wait-Healthy $Services[2] 300)) { exit 1 }

Write-Step 'Demarrage de la Gateway'
Start-ServiceWindow $Services[3]
if (-not (Wait-Healthy $Services[3] 300)) { exit 1 }

Show-Summary
