#!/bin/bash
set -e

echo "Building all microservices..."
./gradlew clean build -x test --no-daemon

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
