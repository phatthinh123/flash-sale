What can be improve:
- Open API docs with generated DTOs&  versioning
- Improve the validation & business logic
- Define better generic response handler
- Improve jwt with mtls 
- Splitting off the service more (user, product, flash sale, account, purchase)
- Add outbox pattern for event publish consistency
- Add lock ownership-safe release in Redis (Lua compare-delete)
- Add richer benchmark scenarios (mixed read/write and purchase-heavy tests)
- Add dashboards/metrics (Prometheus + Grafana)
- Add resilience policies (timeouts, retries, circuit breaker)
- Add SonarQube
# AUTH-SERVICE
Normally OTPs are often stored in memory cached because they're short-lived and reduces load on the main database
However we choose to create an table in this case for few important reasons:
1. Easier to test
2. Extends for audit trail
3. Simplification

Swagger

http://localhost:8081/swagger-ui.html
http://localhost:8082/swagger-ui.html

# Redis stream tour
docker exec -it flashsale-redis redis-cli XLEN flashsale.purchase.completed
docker exec -it flashsale-redis redis-cli XRANGE flashsale.purchase.completed - +

# Inspect the consumer group
docker exec -it flashsale-redis redis-cli XINFO GROUPS flashsale.purchase.completed

# See pending (unacked) messages
docker exec -it flashsale-redis redis-cli XPENDING flashsale.purchase.completed inventory-group