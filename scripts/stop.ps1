# =====================================================================
#  AssetPilot - arret de tous les services
#    .\scripts\stop.ps1           -> arrete Docker et les services Maven locaux
#    .\scripts\stop.ps1 -Reset    -> idem + supprime les volumes Docker (donnees)
# =====================================================================
param([switch]$Reset)

$Root = Split-Path -Parent $PSScriptRoot
$Ports = @(8081, 8082, 8083, 8084, 9090)

Write-Host "`n==> Arret des conteneurs Docker" -ForegroundColor Cyan
if (Get-Command docker -ErrorAction SilentlyContinue) {
    Push-Location $Root
    if ($Reset) { docker compose down -v 2>$null } else { docker compose down 2>$null }
    Pop-Location
    Write-Host '    [OK] docker compose down' -ForegroundColor Green
} else {
    Write-Host '    Docker absent, etape ignoree'
}

Write-Host "`n==> Arret des services Java locaux (ports $($Ports -join ', '))" -ForegroundColor Cyan

# 1. Fenetres ouvertes par start.ps1 (d'abord, sinon leur boucle de relance redemarrerait Maven)
#    (reperees par leur ligne de commande : le titre n'est pas fiable sous Windows Terminal)
Get-CimInstance Win32_Process -Filter "Name='powershell.exe' OR Name='pwsh.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -like '*AssetPilot - *spring-boot:run*' -and $_.ProcessId -ne $PID } |
    ForEach-Object {
        Stop-Process -Id $_.ProcessId -Force -Confirm:$false -ErrorAction SilentlyContinue
        Write-Host "    [OK] fenetre de lancement (PID $($_.ProcessId)) fermee" -ForegroundColor Green
    }

# 2. Applications Java a l'ecoute sur les ports, et leur processus Maven parent
$pids = Get-NetTCPConnection -State Listen -LocalPort $Ports -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique
$stopped = 0
foreach ($p in $pids) {
    $proc = Get-CimInstance Win32_Process -Filter "ProcessId=$p" -ErrorAction SilentlyContinue
    # On n'arrete que les processus Java (pas Docker Desktop ni un autre programme)
    if ($proc -and $proc.Name -eq 'java.exe') {
        $parent = Get-CimInstance Win32_Process -Filter "ProcessId=$($proc.ParentProcessId)" -ErrorAction SilentlyContinue
        if ($parent -and $parent.Name -eq 'java.exe' -and $parent.CommandLine -like '*plexus-classworlds*') {
            Stop-Process -Id $parent.ProcessId -Force -Confirm:$false -ErrorAction SilentlyContinue
        }
        Stop-Process -Id $p -Force -Confirm:$false -ErrorAction SilentlyContinue
        Write-Host "    [OK] java (PID $p) arrete" -ForegroundColor Green
        $stopped++
    }
}
if ($stopped -eq 0) { Write-Host '    Aucun service Java local en cours' }

Write-Host "`nAssetPilot est arrete." -ForegroundColor Green
