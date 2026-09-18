@echo off
setlocal

REM Ensure bin folder exists
if not exist "bin" (
    echo Building project first...
    call build_and_run.bat --cli
    exit /b
)

java -cp bin com.tourist.Main --cli
pause
