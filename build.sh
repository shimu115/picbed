#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "========================================"
echo " PicBed - Build & Start"
echo "========================================"
echo ""

# Check prerequisites
echo "[1/5] Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    echo "[ERROR] Docker not found. Please install Docker."
    exit 1
fi
echo "  $(docker --version)"
echo "  All prerequisites met."
echo ""

# Build backend JAR with Maven container
echo "[2/5] Building backend JAR (Maven container)..."
docker run --rm -v "$SCRIPT_DIR/picbed-server:/build" -w /build maven:3.9-eclipse-temurin-17 mvn package -DskipTests -q
echo "  Backend JAR built successfully."
echo ""

# Build frontend dist with Node container
echo "[3/5] Building frontend dist (Node container)..."
#docker run --rm -v "$SCRIPT_DIR/picbed-web:/build" -w /build node:18-alpine sh -c "rm -rf node_modules && npm install && npm run build"
docker run --rm \
  -v "$SCRIPT_DIR/picbed-web:/build" \
  -w /build \
  node:18 \
  bash -c "
    rm -rf node_modules &&
    npm ci &&
    chmod +x node_modules/.bin/vite &&
    npx vite build
  "
echo "  Frontend dist built successfully."
echo ""

# Build Docker runtime images
echo "[4/5] Building Docker runtime images..."
docker compose build
echo "  Docker images built successfully."
echo ""

# Start containers
echo "[5/5] Starting containers..."
docker compose down
docker compose up -d

echo ""
echo "========================================"
echo " PicBed is running!"
echo " Frontend: http://localhost"
echo " Backend:  http://localhost:8080"
echo "========================================"
