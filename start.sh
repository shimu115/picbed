#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Starting PicBed..."
cd "$SCRIPT_DIR"
docker compose up -d

echo "PicBed is running!"
echo "Frontend: http://localhost"
echo "Backend:  http://localhost:8080"
echo "To stop: docker-compose down"
