# Benchmark Guide

This folder contains k6 benchmark scripts for two goals:

1. **Request throughput** (mixed API traffic)
2. **Transaction throughput** (committed purchases/sec)

## Files

- `load-test.js`: mixed request workload (`active products`, `login`, optional `purchase`)
- `transaction-load-test.js`: purchase-only workload, focused on committed transactions/sec
- `prepare_data.sh`: prepares benchmark datasets (users, tokens, product IDs)

Generated runtime files (ignored by git):
- `login_credentials.json`
- `purchase_tokens.json`
- `product_ids.json`

## Prerequisites

- Docker Compose stack is running
- `k6` is run via Docker image (`grafana/k6`)

Start services:

```bash
cd <project-root>
./start.sh
```

## 1) Prepare Data

### Minimal (browse + login benchmark)

```bash
cd <project-root>
./scripts/benchmark/prepare_data.sh
```

### With purchase dataset

```bash
cd <project-root>
PURCHASE_USERS=200 PURCHASE_PRODUCTS=200 ./scripts/benchmark/prepare_data.sh
```

## 2) Mixed Request Throughput Benchmark

This validates API request throughput (RPS), not strict committed transactions.

### Quick smoke run

```bash
cd <project-root>
docker run --rm --network host \
  -v "$PWD/scripts/benchmark:/scripts" \
  grafana/k6 run /scripts/load-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e BROWSE_RATE=8 \
  -e LOGIN_RATE=2 \
  -e PURCHASE_RATE=0 \
  -e DURATION=10s \
  -e PREALLOCATED_VUS=20 \
  -e MAX_VUS=60
```

### 500 RPS mixed run example

```bash
cd <project-root>
docker run --rm --network host \
  -v "$PWD/scripts/benchmark:/scripts" \
  grafana/k6 run /scripts/load-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e BROWSE_RATE=400 \
  -e LOGIN_RATE=100 \
  -e PURCHASE_RATE=0 \
  -e DURATION=2m \
  -e PREALLOCATED_VUS=300 \
  -e MAX_VUS=1000
```

## 3) Transaction Throughput Benchmark (TPS)

Use this when requirement means **committed business transactions per second**.

`transaction-load-test.js` is strict:
- counts only `status=200` purchase commits as successful transactions
- fails if there are any rejected transactions
- fails if committed TPS is below target threshold

### Prepare enough users/products first

```bash
cd <project-root>
PURCHASE_USERS=1000 PURCHASE_PRODUCTS=200 ./scripts/benchmark/prepare_data.sh
```

For sustained target, size users by duration with buffer:

Required users >= `TX_RATE * test_seconds + TX_RATE`

Example:
- `500 TPS` for `2 seconds` requires at least `1,500` users
- `500 TPS` for `10 seconds` requires at least `5,500` users

### Run transaction benchmark

2-second run

```bash
cd <project-root>
PURCHASE_USERS=1500 PURCHASE_PRODUCTS=300 ./scripts/benchmark/prepare_data.sh

docker run --rm --network host \
  -v "$PWD/scripts/benchmark:/scripts" \
  grafana/k6 run /scripts/transaction-load-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e TX_RATE=500 \
  -e DURATION=2s \
  -e PREALLOCATED_VUS=600 \
  -e MAX_VUS=1500
```

10-second run 

```bash
cd <project-root>
PURCHASE_USERS=5500 PURCHASE_PRODUCTS=600 ./scripts/benchmark/prepare_data.sh

docker run --rm --network host \
  -v "$PWD/scripts/benchmark:/scripts" \
  grafana/k6 run /scripts/transaction-load-test.js \
  -e BASE_URL=http://localhost:8080 \
  -e TX_RATE=500 \
  -e DURATION=10s \
  -e PREALLOCATED_VUS=800 \
  -e MAX_VUS=2000
```

## Interpreting Results

### For `load-test.js`
- `http_reqs`: achieved request throughput
- `http_req_failed`: request error rate
- `http_req_duration` p95: latency at tail

### For `transaction-load-test.js`
- `successful_transactions`: committed transactions/sec (actual TPS)
- `rejected_transactions`: failed/rejected purchase attempts
- Threshold `successful_transactions: rate>=TX_RATE*TPS_TOLERANCE` must pass
- Threshold `rejected_transactions: count==0` must pass
- If users/tokens are insufficient, benchmark stops early with a dataset error

## Important Notes

- Your business rule allows **one purchase per user per day**.
- For strict TPS claims, always prepare enough distinct users first.
