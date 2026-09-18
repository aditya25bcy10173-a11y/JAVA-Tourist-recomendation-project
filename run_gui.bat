@echo off
setlocal

REM Ensure bin folder exists
if not exist "bin" (
    echo Building project first...
    call build_and_run.bat
    exit /b
)

start javaw -cp bin com.tourist.Main
