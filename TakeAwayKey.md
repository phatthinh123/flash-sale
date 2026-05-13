What can be improve:
- Open API docs with generated DTOs & versioning
- Improve the validation
- Define generic response handler
- Improve jwt with mtls 
- Splitting off the service more (user, auth, account)
# AUTH-SERVICE
Normally OTPs are often stored in memory cached because they're short-lived and reduces load on the main database
However we choose to create an table in this case for few important reasons:
1. Easier to test
2. Extends for audit trail
3. Simplification

Swagger

http://localhost:8081/swagger-ui.html
http://localhost:8082/swagger-ui.html
