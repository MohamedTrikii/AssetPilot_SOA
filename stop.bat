@echo off
REM AssetPilot - arret de tous les services. "stop.bat reset" supprime aussi les donnees Docker.
title AssetPilot - arret
if /I "%~1"=="reset" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\stop.ps1" -Reset
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\stop.ps1"
)
pause
