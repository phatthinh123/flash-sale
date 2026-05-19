# Flash Sale Backend

Multi-service backend for authentication, flash sale purchasing, and inventory synchronization.

## Services

- `gateway` (`:8080`): API entrypoint and routing
- `auth-service` (`:8081`): register/login/logout + OTP verification
- `flashsale-service` (`:8082`): active products and purchase flow
- `inventory-service` (`:8083`): inventory view + purchase event consumption
- `postgres` (`:5432`): persistence
- `redis` (`:6379`): distributed lock + Redis Streams

## Architecture Notes

- `auth-service` and `flashsale-service` share `auth_db`.
- `inventory-service` uses `inventory_db`.
- Flyway history is separated per service (`auth_flyway_schema_history`, `flashsale_flyway_schema_history`) to avoid migration conflicts.

## Prerequisites

- Docker + Docker Compose
- (Optional for local non-Docker run) Java 21

## 1) Setup and Start

From project root (recommended):

```bash
cd <project-root>
./start.sh
```

Manual equivalent:

```bash
cd <project-root>
docker compose up -d --build
```

Stop everything:

```bash
docker compose down
```

Stop and reset all data volumes:

```bash
docker compose down -v
```

## 2) API Base URLs

- Through gateway (recommended): `http://localhost:8080`
- Direct service URLs:
  - `http://localhost:8081` (auth)
  - `http://localhost:8082` (flash sale)
  - `http://localhost:8083` (inventory)

The demo below uses the gateway URL.

## 3) Bruno Collection (Step-by-Step)

Bruno collection path:
- `bruno/Flash-Sale-Backend`

### Step 0: Open collection

1. Open Bruno
2. `Open Collection`
3. Select `bruno/Flash-Sale-Backend`
4. Active environment `local`

### Step 1: Set environment values

In `local` environment:
- `baseUrl`: `http://localhost:8080`
- `email`: `demo.user@example.com`
- `password`: `Password@123`
- Keep `userId`, `otpCode`, `jwtToken`, `productId` empty for now

### Step 2: Run auth flow

Run requests in this order:
1. `Auth/Register`
2. Get OTP from logs:

```bash
docker compose logs --tail=200 auth-service | grep -A2 "OTP VERIFICATION CODE"
```

3. Copy `userId` from Register response into env `userId`
4. Copy OTP value into env `otpCode`
5. Run `Auth/Verify OTP`
6. Run `Auth/Login`
7. Copy `token` from Login response into env `jwtToken`

### Step 3: Prepare demo product (if needed)

If `Flash Sale/Get Active Products` returns an empty array, seed one product:

```bash
SLOT_ID=$(cat /proc/sys/kernel/random/uuid)
PRODUCT_ID=$(cat /proc/sys/kernel/random/uuid)

docker exec -i flashsale-postgres psql -U postgres -d auth_db <<SQL
INSERT INTO flash_sale_slots (id, sale_date, start_time, end_time, active)
VALUES ('$SLOT_ID', CURRENT_DATE, '00:00:00', '23:59:59', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO flash_sale_products (
  id, slot_id, product_name, description,
  original_price, flash_price, total_quantity, remaining_quantity, version
) VALUES (
  '$PRODUCT_ID', '$SLOT_ID', 'Demo Product', 'Demo item for flash sale',
  100.00, 75.00, 10, 10, 0
)
ON CONFLICT (id) DO NOTHING;
SQL

#  inventory_db
docker exec -i flashsale-postgres psql -U postgres -d inventory_db <<SQL
INSERT INTO inventory (product_id, product_name, stock_quantity)
VALUES ('$PRODUCT_ID', 'Demo Product', 100)
ON CONFLICT (product_id) DO NOTHING;
SQL

echo "PRODUCT_ID=$PRODUCT_ID"
```

Set env `productId` from the command output (or from active-products response).

### Step 4: Run purchase + inventory flow

Run requests:
1. `Flash Sale/Get Active Products`
2. `Flash Sale/Purchase Product`
3. `Inventory/Get Inventory`

## 4) Main APIs

Auth:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/verify-otp`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout` (requires JWT)

Flash sale:
- `GET /api/v1/flash-sales/active`
- `POST /api/v1/flash-sales/{productId}/purchase` (requires JWT)

Inventory:
- `GET /api/v1/inventory`

## 5) Benchmark

For benchmark setup and run commands, see:

- `scripts/benchmark/README.md`
