@echo off
chcp 65001 >nul
setlocal

echo Starting PicBed...
docker-compose up -d

if %errorlevel% equ 0 (
    echo PicBed is running!
    echo Frontend: http://localhost
    echo Backend:  http://localhost:8080
    echo To stop: docker-compose down
) else (
    echo [ERROR] Failed to start.
    exit /b 1
)
