# Backend Take-Home Assignment: Flash Sale Service

## 1) Objective
Build a backend service with Java and Spring Boot for authentication and flash sale operations.

The solution should be:
- Secure
- High throughput
- Stable in a multi-instance environment
- Easy to extend

You are free to design the architecture, data model, and APIs as long as the requirements below are met.

## 2) Scope

### 2.1 Authentication (Register / Login / Logout)
Implement authentication for both email and phone number.

Requirements:
- One API per function (register, login, logout); email/phone is identified from input
- OTP verification to validate email or phone (delivery can be mocked via logs, DB, or outbox)
- Protect user data and avoid exposing sensitive information

### 2.2 Flash Sale
Build a flash sale program with multiple time slots per day.

Each slot must include:
- Product list
- Quantity limit
- Sale price

Rules:
- Flash sale configuration must be stored in a database (no hardcoded configuration)
- Provide API to get products currently in flash sale
- Provide API to purchase within a valid slot only

Assumption:
- Users already have balance in the system

Hard constraints:
- Never oversell configured quantity
- Each user can buy only one flash sale product per day
- Must remain correct under concurrent requests

### 2.3 Inventory Synchronization
Implement a mechanism to sync product and purchase changes with inventory.

The synchronization must ensure:
- Idempotency (no duplicate processing)
- Data consistency

## 3) Non-Functional Requirements

### 3.1 Security
- Follow basic backend security best practices
- Protect critical APIs appropriately

### 3.2 Performance
- Target at least 500 TPS under reasonable conditions

### 3.3 Scalability
- Service should run well in a multi-instance environment
- Do not rely on local in-memory state per instance
- Actual multi-instance deployment is not required, but design must support it

### 3.4 Extensibility
Code should be clean, readable, and easy to extend:
- Add new products easily
- Extend flash sale conditions/rules
- Add new APIs over time

## 4) Development Environment
Provide Docker/Docker Compose so team members can run the full system without installing extra dependencies.

README must include clear local setup instructions.

## 5) Submission
- Publish source code to a public GitHub repository
- README must describe:
  - Build and run instructions
  - Main APIs
  - Design assumptions

You are encouraged to extend the solution with additional products, APIs, and business rules.

## 6) Presentation
If you pass the coding round, be ready to present your solution.

Presentation should cover:
- Overall architecture
- Concurrency handling strategy
- How quantity correctness is guaranteed
- Future extensibility approach

## 7) Notes
- UI is not required
- Technology is flexible as long as Java + Spring Boot is used
- Design quality, correctness, and scalability are more important than feature count
