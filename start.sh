#!/bin/bash
set -e

# Always run from project root (directory containing this script)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker command not found. Please install Docker first."
  exit 1
fi

echo "Starting infrastructure and microservices with Docker Compose..."
docker compose up --build -d

echo ""
echo "🚀 Flash Sale Application is starting up!"
echo "It may take a minute for all Java services to be healthy."
echo ""
echo "API Gateway: http://localhost:8080"
echo "  - Auth: POST /api/v1/auth/register, /api/v1/auth/login"
echo "  - Flash Sale: GET /api/v1/flash-sales/active"
echo "  - Inventory: GET /api/v1/inventory"
echo ""
echo "To view logs, run: docker compose logs -f"
echo "To stop, run: docker compose down"
