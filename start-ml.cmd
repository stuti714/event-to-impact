@echo off
setlocal
cd /d "%~dp0"
if not exist ".venv\Scripts\python.exe" (
    echo Run setup-ml.cmd first from the folder containing this file.
    exit /b 1
)
echo Starting Event to Impact ML on http://127.0.0.1:8001
".venv\Scripts\python.exe" -m uvicorn ml_service.main:app --host 127.0.0.1 --port 8001
