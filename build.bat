@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set "ROOT=%~dp0"

echo ========================================
echo  PicBed - Build ^& Start
echo ========================================
echo.

REM Check prerequisites
echo [1/5] Checking prerequisites...

where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker not found. Please install Docker.
    exit /b 1
)
for /f "tokens=*" %%i in ('docker --version 2^>^&1') do echo   %%i
echo   All prerequisites met.
echo.

REM Build backend JAR with Maven container
echo [2/5] Building backend JAR (Maven container)...
docker run --rm -v "%ROOT%picbed-server:/build" -w /build maven:3.9-eclipse-temurin-17 mvn package -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Backend build failed.
    exit /b 1
)
echo   Backend JAR built successfully.
echo.

REM Build frontend dist with Node container
echo [3/5] Building frontend dist (Node container)...
docker run --rm -v "%ROOT%picbed-web:/build" -w /build node:18-alpine sh -c "rm -rf node_modules && npm install && npm run build"
if %errorlevel% neq 0 (
    echo [ERROR] Frontend build failed.
    exit /b 1
)
echo   Frontend dist built successfully.
echo.

REM Build Docker runtime images
echo [4/5] Building Docker runtime images...
docker-compose build
if %errorlevel% neq 0 (
    echo [ERROR] Docker build failed.
    exit /b 1
)
echo   Docker images built successfully.
echo.

REM Start containers
echo [5/5] Starting containers...
docker-compose up -d
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start containers.
    exit /b 1
)

echo.
echo ========================================
echo  PicBed is running!
echo  Frontend: http://localhost
echo  Backend:  http://localhost:8080
echo ========================================

pause .
