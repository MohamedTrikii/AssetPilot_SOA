@echo off
REM AssetPilot - lancement (double-clic). Options : start.bat docker | start.bat local
title AssetPilot - lancement
if "%~1"=="" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start.ps1"
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start.ps1" -Mode %1
)
pause
