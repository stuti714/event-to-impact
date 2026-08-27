@echo off
setlocal
cd /d "%~dp0"
echo Creating the Event to Impact ML environment...
if not exist ".venv\Scripts\python.exe" (
    py -3 -m venv .venv 2>nul || python -m venv .venv
)
if not exist ".venv\Scripts\python.exe" (
    echo Could not create .venv. Install Python 3.10 or newer and try again.
    exit /b 1
)
".venv\Scripts\python.exe" -m pip install --upgrade pip
if errorlevel 1 exit /b 1
".venv\Scripts\python.exe" -m pip install -r ml_service\requirements.txt
if errorlevel 1 exit /b 1
".venv\Scripts\python.exe" -m pytest ml_service\test_recommender.py -q
if errorlevel 1 exit /b 1
echo.
echo ML environment ready. Run start-ml.cmd in a new terminal.
