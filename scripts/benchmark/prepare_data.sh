#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
LOGIN_EMAIL="${LOGIN_EMAIL:-loadtest.login.$(date +%s)@example.com}"
LOGIN_PASSWORD="${LOGIN_PASSWORD:-Password@123}"
PURCHASE_USERS="${PURCHASE_USERS:-0}"
PURCHASE_PRODUCTS="${PURCHASE_PRODUCTS:-0}"
PURCHASE_USER_MODE="${PURCHASE_USER_MODE:-direct}"
JWT_SECRET_B64="${JWT_SECRET_B64:-dmVyeV9zZWNyZXRfa2V5X3RoYXRfaXNfbG9uZ19lbm91Z2hfZm9yX2hzMjU2X2FsZ29yaXRobV8xMjM0NQ==}"
TOKEN_TTL_SECONDS="${TOKEN_TTL_SECONDS:-3600}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$PROJECT_ROOT"

register_user() {
  local email="$1"
  local password="$2"

  curl -s -X POST "$BASE_URL/api/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"phone\":null,\"password\":\"$password\"}"
}

parse_json_field() {
  local json="$1"
  local field="$2"
  python3 - "$json" "$field" <<'PY'
import json
import sys
try:
    data = json.loads(sys.argv[1])
    value = data.get(sys.argv[2])
    print(value if isinstance(value, str) else '')
except Exception:
    print('')
PY
}

verify_otp_if_registered() {
  local user_id="$1"
  if [ -z "$user_id" ]; then
    return 0
  fi

  local otp
  otp="$(docker exec -i flashsale-postgres psql -U postgres -d auth_db -t -A -c "SELECT otp_code FROM otp_tokens WHERE user_id = '$user_id' ORDER BY created_at DESC LIMIT 1;")"
  otp="$(echo "$otp" | tr -d '[:space:]')"

  if [ -n "$otp" ]; then
    curl -s -X POST "$BASE_URL/api/v1/auth/verify-otp" \
      -H "Content-Type: application/json" \
      -d "{\"userId\":\"$user_id\",\"otpCode\":\"$otp\"}" >/dev/null
  fi
}

login_token() {
  local identifier="$1"
  local password="$2"
  local response
  response="$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"identifier\":\"$identifier\",\"password\":\"$password\"}")"
  parse_json_field "$response" "token"
}

generate_jwt_token() {
  local user_id="$1"
  python3 - "$JWT_SECRET_B64" "$user_id" "$TOKEN_TTL_SECONDS" <<'PY'
import base64
import hashlib
import hmac
import json
import sys
import time

secret = base64.b64decode(sys.argv[1])
sub = sys.argv[2]
ttl = int(sys.argv[3])
now = int(time.time())

header = {"alg": "HS256", "typ": "JWT"}
payload = {"sub": sub, "iat": now, "exp": now + ttl}

def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")

header_b64 = b64url(json.dumps(header, separators=(",", ":")).encode("utf-8"))
payload_b64 = b64url(json.dumps(payload, separators=(",", ":")).encode("utf-8"))
signing_input = f"{header_b64}.{payload_b64}".encode("ascii")
signature = hmac.new(secret, signing_input, hashlib.sha256).digest()

print(f"{header_b64}.{payload_b64}.{b64url(signature)}")
PY
}

mkdir -p "$SCRIPT_DIR"

echo "[prepare] Creating/ensuring login benchmark user..."
register_resp="$(register_user "$LOGIN_EMAIL" "$LOGIN_PASSWORD")"
login_user_id="$(parse_json_field "$register_resp" "userId")"
verify_otp_if_registered "$login_user_id"

cat > "$SCRIPT_DIR/login_credentials.json" <<JSON
[
  {
    "identifier": "$LOGIN_EMAIL",
    "password": "$LOGIN_PASSWORD"
  }
]
JSON

echo "[prepare] Wrote $SCRIPT_DIR/login_credentials.json"

echo "[prepare] Creating optional purchase dataset users=$PURCHASE_USERS products=$PURCHASE_PRODUCTS"

tokens_file="$SCRIPT_DIR/purchase_tokens.json"
products_file="$SCRIPT_DIR/product_ids.json"

echo "[]" > "$tokens_file"
echo "[]" > "$products_file"

if [ "$PURCHASE_PRODUCTS" -gt 0 ]; then
  product_ids=()
  slot_id="$(cat /proc/sys/kernel/random/uuid)"

  docker exec -i flashsale-postgres psql -U postgres -d auth_db <<SQL
INSERT INTO flash_sale_slots (id, sale_date, start_time, end_time, active)
VALUES ('$slot_id', CURRENT_DATE, '00:00:00', '23:59:59', TRUE)
ON CONFLICT (id) DO NOTHING;
SQL

  auth_sql_file="$(mktemp)"
  inventory_sql_file="$(mktemp)"

  for ((i=1; i<=PURCHASE_PRODUCTS; i++)); do
    product_id="$(cat /proc/sys/kernel/random/uuid)"
    product_name="Load Test Product $i"
    product_ids+=("$product_id")

    cat >> "$auth_sql_file" <<SQL
INSERT INTO flash_sale_products (
  id, slot_id, product_name, description,
  original_price, flash_price, total_quantity, remaining_quantity, version
) VALUES (
  '$product_id', '$slot_id', '$product_name', 'Used for purchase benchmark',
  100.00, 75.00, 100000, 100000, 0
)
ON CONFLICT (id) DO NOTHING;
SQL

    cat >> "$inventory_sql_file" <<SQL
INSERT INTO inventory (product_id, product_name, stock_quantity)
VALUES ('$product_id', '$product_name', 100000)
ON CONFLICT (product_id) DO NOTHING;
SQL
  done

  docker exec -i flashsale-postgres psql -q -U postgres -d auth_db < "$auth_sql_file"
  docker exec -i flashsale-postgres psql -q -U postgres -d inventory_db < "$inventory_sql_file"
  rm -f "$auth_sql_file" "$inventory_sql_file"

  python3 - <<'PY' "$products_file" "${product_ids[@]}"
import json
import sys
path = sys.argv[1]
ids = sys.argv[2:]
with open(path, 'w', encoding='utf-8') as f:
    json.dump(ids, f)
PY

  echo "[prepare] Wrote $products_file"
fi

if [ "$PURCHASE_USERS" -gt 0 ]; then
  purchase_tokens=()
  email_prefix="loadtest.purchase.$(date +%s%N)"

  if [ "$PURCHASE_USER_MODE" = "direct" ]; then
    users_sql_file="$(mktemp)"

    for ((i=1; i<=PURCHASE_USERS; i++)); do
      user_id="$(cat /proc/sys/kernel/random/uuid)"
      email="$email_prefix.$i@example.com"

      cat >> "$users_sql_file" <<SQL
INSERT INTO users (
  id, email, phone, password_hash, balance,
  email_verified, phone_verified, created_at, updated_at
) VALUES (
  '$user_id', '$email', NULL, 'benchmark-hash', 1000000.00,
  TRUE, FALSE, NOW(), NOW()
)
ON CONFLICT (email) DO NOTHING;
SQL

      purchase_tokens+=("$(generate_jwt_token "$user_id")")
    done

    docker exec -i flashsale-postgres psql -q -U postgres -d auth_db < "$users_sql_file"
    rm -f "$users_sql_file"
  else
    for ((i=1; i<=PURCHASE_USERS; i++)); do
      email="$email_prefix.$i@example.com"
      resp="$(register_user "$email" "$LOGIN_PASSWORD")"
      user_id="$(parse_json_field "$resp" "userId")"
      verify_otp_if_registered "$user_id"
      token="$(login_token "$email" "$LOGIN_PASSWORD")"
      if [ -n "$token" ]; then
        purchase_tokens+=("$token")
      fi
    done
  fi

  python3 - <<'PY' "$tokens_file" "${purchase_tokens[@]}"
import json
import sys
path = sys.argv[1]
tokens = sys.argv[2:]
with open(path, 'w', encoding='utf-8') as f:
    json.dump(tokens, f)
PY

  echo "[prepare] Wrote $tokens_file"
fi

echo "[prepare] Done"

