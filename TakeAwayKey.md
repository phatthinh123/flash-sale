# Flash Sale - Takeaway Notes

## What Is Implemented
- Authentication with either email or phone (single API path), OTP verification, JWT login/logout.
- Flash-sale correctness controls: product lock, optimistic update, and one-purchase-per-day rule.
- Inventory synchronization via Redis Streams consumer group with idempotency (`processed_orders`).
- Dockerized multi-service setup (`gateway`, `auth-service`, `flashsale-service`, `inventory-service`, `postgres`, `redis`).

## Important Design Decisions

### OTP persistence in DB (instead of cache only)
Common systems keep OTP in memory/Redis because OTP is short-lived. Here OTP is stored in DB for:
1. Easier deterministic testing and inspection
2. Better auditability and traceability
3. Simpler local debugging and demo flow

### Inventory sync delivery model
- Redis Streams consumer group gives at-least-once delivery.
- Duplicate delivery is handled safely through idempotency in inventory processing.

## Useful Local Links
- `http://localhost:8081/swagger-ui.html` (auth-service)
- `http://localhost:8082/swagger-ui.html` (flashsale-service)
- `http://localhost:8083/swagger-ui.html` (inventory-service, if enabled)

## Redis Streams Quick Debug Commands

### Stream size and records
```bash
docker exec -it flashsale-redis redis-cli XLEN flashsale.purchase.completed
docker exec -it flashsale-redis redis-cli XRANGE flashsale.purchase.completed - +
```

### Consumer groups
```bash
docker exec -it flashsale-redis redis-cli XINFO GROUPS flashsale.purchase.completed
```

### Pending (unacked) messages
```bash
docker exec -it flashsale-redis redis-cli XPENDING flashsale.purchase.completed inventory-group
```

## Prioritized Improvements (Next Steps)

### High impact (correctness/reliability)
- Add outbox pattern for stronger publish/commit consistency.
- Add lock ownership-safe release in Redis (Lua compare-delete).
- Harden validation and business rule boundaries.

### Medium impact (operability/performance)
- Add richer benchmark scenarios (mixed read/write and purchase-heavy tests).
- Add observability stack (Prometheus + Grafana + alerting).
- Add resilience policies (timeouts, retries, circuit breaker).

### Engineering quality
- Standardize API response envelope and error model.
- Improve OpenAPI docs (DTO generation/versioning).
- Add static analysis gates (e.g., SonarQube).
- Consider further service decomposition (`user`, `product`, `account`, `purchase`) when scale requires it.
- Further extract common classes