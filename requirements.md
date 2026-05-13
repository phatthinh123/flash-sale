1. General Objective
   Build a secure, scalable, and multi-instance-ready backend service using Java + Spring Boot. Priority is given to design thinking, correctness, and scalability over just adding features.

2. Authentication
   APIs: Register, Login, and Logout.
   Identifier: Must support both Email and Phone numbers using a single API (distinguished by the input).
   OTP: Include an OTP verification step (the actual sending can be mocked via logs/DB).
   Security: Ensure user data is secure and sensitive information (like passwords) is not exposed.

3. Flash Sale Core
   Structure: Multiple time slots throughout the day, each containing specific products, quantity limits, and flash prices.
   Storage: The entire flash sale configuration must be managed in a database, not hardcoded.
   APIs:
   Retrieve the list of products currently on flash sale.
   Purchase a product during a valid time slot.
   Constraints (The Hard Part):
   Assume users already have a balance.
   NEVER exceed the configured quantity (No overselling).
   Each user can only purchase 1 flash sale product per day.
   Ensure absolute correctness even with multiple simultaneous requests.
4. Inventory Synchronization
   Build a mechanism to synchronize purchase changes with the inventory.
   Must guarantee no duplicate processing (idempotency) and consistent data.
5. Non-Functional Requirements
   Performance: Must handle at least 500 TPS.
   Scalability: The code must be stateless so it can run safely in a multi-instance (horizontal scaling) environment.
   Extensibility: Code must be clean so new products, conditions, and APIs can be easily added later.
6. Deliverables & DevOps
   Docker Compose: Must provide a Docker environment so the reviewers can run the entire system without installing dependencies.
   README: Must include clear instructions on how to build and run, list the main APIs, and document any design assumptions.
   No UI: A user interface is strictly not required.