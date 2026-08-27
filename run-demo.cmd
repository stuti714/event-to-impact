@echo off
setlocal
cd /d "%~dp0"
if not exist ".venv\Scripts\python.exe" (
    echo The ML environment is not ready.
    echo Run setup-ml.cmd once, then run this file again.
    exit /b 1
)
start "Event to Impact ML - keep open" cmd /k call "%~dp0start-ml.cmd"
timeout /t 3 /nobreak >nul
echo Starting Event to Impact web application on http://localhost:8082
call mvnw.cmd spring-boot:run
