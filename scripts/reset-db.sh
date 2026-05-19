#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

MODE="${1:---full}"
NO_CONFIRM="${NO_CONFIRM:-false}"

usage() {
  cat <<'EOF'
Usage: ./scripts/reset-db.sh [--full|--data-only|--help]

Modes:
  --full       Stop stack, remove volumes, and start fresh (destructive)
  --data-only  Keep containers/volumes, truncate app tables only

Environment:
  NO_CONFIRM=true   Skip confirmation prompt

Examples:
  ./scripts/reset-db.sh --full
  ./scripts/reset-db.sh --data-only
  NO_CONFIRM=true ./scripts/reset-db.sh --full
EOF
}

confirm() {
  if [ "$NO_CONFIRM" = "true" ]; then
    return 0
  fi

  read -r -p "This will delete data (${MODE}). Continue? [y/N] " ans
  case "$ans" in
    y|Y|yes|YES) ;;
    *)
      echo "Aborted."
      exit 0
      ;;
  esac
}

full_reset() {
  echo "[reset-db] Running full reset (down -v + up -d --build)..."
  cd "$PROJECT_ROOT"
  docker compose down -v
  docker compose up -d --build
  echo "[reset-db] Full reset completed."
}

data_only_reset() {
  echo "[reset-db] Running data-only reset (truncate tables)..."

  if ! docker ps --format '{{.Names}}' | grep -q '^flashsale-postgres$'; then
    echo "[reset-db] Error: container 'flashsale-postgres' is not running."
    echo "[reset-db] Start stack first with: ./start.sh"
    exit 1
  fi

  docker exec -i flashsale-postgres psql -U postgres -d auth_db <<'SQL'
TRUNCATE TABLE purchase_orders, flash_sale_products, flash_sale_slots, otp_tokens, users RESTART IDENTITY CASCADE;
SQL

  docker exec -i flashsale-postgres psql -U postgres -d inventory_db <<'SQL'
TRUNCATE TABLE processed_orders, inventory RESTART IDENTITY CASCADE;
SQL

  echo "[reset-db] Data-only reset completed."
}

case "$MODE" in
  --help|-h)
    usage
    exit 0
    ;;
  --full|--data-only)
    ;;
  *)
    echo "Unknown option: $MODE"
    usage
    exit 1
    ;;
esac

confirm

case "$MODE" in
  --full)
    full_reset
    ;;
  --data-only)
    data_only_reset
    ;;
esac

