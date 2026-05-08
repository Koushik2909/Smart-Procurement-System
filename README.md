# 🏗️ Smart Procurement System

A full-stack, multi-module **Spring Boot** backend for managing the end-to-end government/enterprise procurement lifecycle — from tender publication to contract payment — secured with JWT authentication, fraud detection, and role-based access control.

---

## 📋 Table of Contents
1. [Project Architecture](#project-architecture)
2. [Modules Overview](#modules-overview)
3. [Technology Stack](#technology-stack)
4. [Getting Started](#getting-started)
5. [Authentication & Roles](#authentication--roles)
6. [The Full Procurement Workflow](#the-full-procurement-workflow)
7. [Module 4: Bid Evaluation System](#module-4-bid-evaluation-system)
8. [Module 10: Fraud & Collusion Detection](#module-10-fraud--collusion-detection)
9. [Vendor Blocklist System](#vendor-blocklist-system)
10. [Auction Engine](#auction-engine)
11. [Contract & Payments](#contract--payments)
12. [Audit & Compliance](#audit--compliance)
13. [GraphQL API Reference](#graphql-api-reference)
14. [Running Tests](#running-tests)
15. [Error Handling](#error-handling)

---

## Project Architecture

The system is a **Maven multi-module monorepo**. All modules share the same Spring Boot application context when the `application-runner` module starts up.

```
procurement-system/
├── core-common/           # Shared base entities, exceptions
├── security-auth/         # JWT auth, user roles, blocklist
├── tender-management/     # Tender CRUD and lifecycle
├── vendor-management/     # Vendor KYC registration & approval
├── bid-management/        # Bid submission, evaluation, scoring
├── fraud-management/      # Fraud/collusion detection & blocklist actions
├── auction-engine/        # Reverse auction with virtual thread monitoring
├── contract-financial/    # Contract lifecycle and payment schedules
├── compliance-audit/      # Audit logging and compliance reporting
├── notification-service/  # Notification dispatch
└── application-runner/    # Spring Boot entry point, GraphQL schema, global exception handler
```

---

## Modules Overview

| Module | Purpose |
|---|---|
| `core-common` | Shared `BaseEntity`, `ResourceNotFoundException`, `UnauthorizedException` |
| `security-auth` | JWT generation/validation, user registration/login, role management, `FraudBlocklist` entity |
| `tender-management` | Create, publish, close tenders; manages `technicalWeight` and `financialWeight` for scoring |
| `vendor-management` | Vendor KYC profile registration with admin approval workflow |
| `bid-management` | Bid submission with blocklist guard; multi-evaluator technical and financial scoring; `BidEvaluation` entity |
| `fraud-management` | Fraud analytics (collusion, cartel patterns, price anomalies); `blockUser` / `unblockUser` mutations |
| `auction-engine` | Live reverse auctions with auto-close via Java Virtual Threads |
| `contract-financial` | Award contracts, digital signature, payment milestone scheduling, penalty processing |
| `compliance-audit` | Append-only audit log with violation flagging and compliance reporting |
| `notification-service` | Send system notifications by role |
| `application-runner` | Spring Boot main class, GraphQL schema, global exception handler |

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 25 (Preview) | Language (uses switch expressions, virtual threads) |
| Spring Boot 3 / Spring 7 | Core framework |
| Spring GraphQL | API layer (replaces REST) |
| Spring Security + JWT | Stateless authentication |
| Spring Data JPA + Hibernate | ORM and database layer |
| MySQL | Relational database |
| JUnit 5 + Mockito | Unit testing |
| Maven (Multi-module) | Build system |

---

## Getting Started

### Prerequisites
- Java 25 JDK
- MySQL running locally
- Maven 3.9+ installed

### 1. Configure Database
Update `application-runner/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/procurement_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

### 2. Build & Run
```bash
mvn clean install
mvn spring-boot:run -pl application-runner
```

### 3. Access GraphQL Playground
Open your browser: [http://localhost:8080/graphiql](http://localhost:8080/graphiql)

---

## Authentication & Roles

All endpoints are secured by JWT. Obtain a token by registering and logging in.

### Register a User
```graphql
mutation {
  register(username: "alice", password: "pass123", role: "VENDOR") {
    token
  }
}
```

### Login
```graphql
mutation {
  login(username: "alice", password: "pass123") {
    token
  }
}
```

Pass the token in every subsequent request header:
```
Authorization: Bearer <your_token_here>
```

### Roles & Permissions

| Role | Permissions |
|---|---|
| `ADMIN` | Full access to all operations including overrides, blocklisting, contract awards |
| `PROCUREMENT_OFFICER` | Create/manage tenders, assign evaluators |
| `EVALUATOR` | Score bids (technical and financial) |
| `VENDOR` | Submit bids, register vendor profiles |
| `AUDITOR` | Read-only access to audit logs and fraud alerts |
| `FINANCE` | Manage payment schedules and releases |

---

## The Full Procurement Workflow

Here is the step-by-step lifecycle of a procurement event, from start to finish:

```
1. ADMIN/OFFICER  → createTender(...)         # Publish a new tender
2. VENDOR         → registerVendorProfile(...) # Register KYC documents
3. ADMIN          → approveVendor(vendorId)    # Approve vendor KYC
4. VENDOR         → submitBid(...)             # Submit sealed proposal
5. OFFICER        → assignEvaluator(bidId, evaluatorId) # Assign evaluators
6. EVALUATOR      → evaluateTechnicalBid(...)  # Score technical proposal
7. EVALUATOR      → evaluateFinancialBid(...)  # Score financial proposal
8. OFFICER        → finalizeEvaluation(bidId)  # Calculate weighted final score
9. ADMIN          → compareBids(tenderId)       # Rank all bids
10. ADMIN         → awardContract(...)         # Award to winning vendor
11. VENDOR        → signContract(...)          # Vendor digitally signs
12. ADMIN         → activateContract(...)      # Contract goes live
13. FINANCE       → createPaymentSchedule(...) # Set payment milestones
14. FINANCE       → releasePayment(paymentId)  # Pay vendor per milestone
```

---

## Module 4: Bid Evaluation System

Bids are scored using a **weighted multi-evaluator system**. Multiple independent evaluators score each bid, and the final score is calculated as a weighted average.

### Formula
```
Final Score = (w1 × Avg Technical Score) + (w2 × Avg Financial Score)
```
- Default weights: **w1 = 0.7** (Technical), **w2 = 0.3** (Financial)
- Weights are configurable per tender via `createTender`

### Key Business Rules
- **Conflict of Interest**: An evaluator who is also a vendor **cannot** evaluate their own bid. The system will throw an error.
- **Independent Inputs**: Multiple evaluators submit scores independently. `finalizeEvaluation` averages all inputs.
- **Admin Override**: An ADMIN can call `overrideEvaluation` to manually set a final score with a justification.

### Example
```graphql
# Step 1: Assign an evaluator
mutation {
  assignEvaluator(bidId: 1, evaluatorId: 5) { id }
}

# Step 2: Evaluator scores the technical proposal
mutation {
  evaluateTechnicalBid(bidId: 1, evaluatorId: 5, score: 85.0) { id }
}

# Step 3: Evaluator scores the financial proposal
mutation {
  evaluateFinancialBid(bidId: 1, evaluatorId: 5, score: 92.0) { id }
}

# Step 4: Officer finalizes and calculates the weighted score
mutation {
  finalizeEvaluation(bidId: 1) { id finalScore status }
}
```

---

## Module 10: Fraud & Collusion Detection

The system runs **three automatic fraud detection algorithms** on bidding data. All queries require `ADMIN` or `AUDITOR` role.

### 1. Bid Collusion Detection
Flags vendors who submitted **suspiciously similar bids** on the same tender (within 5% of each other — a sign of price-fixing).
```graphql
query {
  detectBidCollusion(tenderId: 1)
}
```

### 2. Vendor Cartel Pattern Analysis
Analyzes all historical data to find **pairs of vendors that always bid together** on multiple tenders — a sign of a coordinated cartel.
```graphql
query {
  analyzeVendorPatterns
}
```

### 3. Price Anomaly Detection
Uses **statistical standard deviation** to flag bids that are more than 2 standard deviations away from the group average — covering both extreme undercutting and inflated "cover bids."
```graphql
query {
  getFraudAlerts(tenderId: 1)
}
```

---

## Vendor Blocklist System

When fraud is detected, an ADMIN can permanently block a vendor from submitting any future bids.

### Block a Vendor
```graphql
mutation {
  blockUser(userId: 2, reason: "Bid collusion detected on Tender #10") {
    id userId status reason
  }
}
```

### Unblock a Vendor
```graphql
mutation {
  unblockUser(userId: 2) {
    id status
  }
}
```

### How Enforcement Works
When a blocked vendor tries to submit a bid, the system immediately rejects the request:
```json
{
  "errors": [{
    "message": "Access Denied: Vendor is blocklisted for: Bid collusion detected on Tender #10",
    "extensions": { "classification": "INTERNAL_ERROR" }
  }]
}
```
The blocklist check happens **before the bid is saved to the database** — nothing slips through.

---

## Auction Engine

A reverse auction where vendors compete by submitting **decreasing** bids. A virtual thread auto-monitors the deadline and closes the auction automatically.

```graphql
# Start auction
mutation {
  startAuction(input: {
    tenderId: 1,
    startingPrice: 1000000.0,
    minimumDecrement: 5000.0,
    startTime: "2026-06-01T09:00:00",   # MUST use ISO format: YYYY-MM-DDThh:mm:ss
    endTime: "2026-06-01T17:00:00"
  }) { id status }
}

# Place a bid
mutation {
  placeBid(auctionId: 1, amount: 995000.0) { id bidAmount }
}

# View the leaderboard
query {
  getLeaderboard(auctionId: 1) { vendorId bidAmount bidTime }
}
```

> ⚠️ **Date format**: Always use `YYYY-MM-DDThh:mm:ss` format (e.g. `"2026-06-01T10:30:00"`). A time-only string like `"10:09:10"` will cause a parse error.

---

## Contract & Payments

### Contract Lifecycle
```
GENERATED → SIGNED → ACTIVE → TERMINATED
```

```graphql
mutation { awardContract(tenderId: 1, vendorId: 2, contractValue: 500000.0) { id status } }
mutation { signContract(id: 1, digitalSignature: "VENDOR_SIGN_XYZ") { id status signedAt } }
mutation { activateContract(id: 1) { id status } }
mutation { terminateContract(id: 1, reason: "Scope change") { id status } }
```

### Payment Lifecycle
```
SCHEDULED → RELEASED
                └─→ PENALTY_APPLIED
                └─→ REFUNDED
```

```graphql
mutation { createPaymentSchedule(contractId: 1, milestoneName: "Phase 1", amount: 100000.0, dueDate: "2026-07-01T00:00:00") { id status } }
mutation { releasePayment(paymentId: 1) { id status releasedAt } }
mutation { processPenalty(paymentId: 1, penaltyAmount: 5000.0) { id penaltyAmount status } }
```

---

## Audit & Compliance

Every significant action in the system can be logged for compliance purposes.

```graphql
# Log an event
mutation {
  logAuditEvent(eventType: "CONTRACT_AWARDED", entityType: "Contract", entityId: 1, details: "Awarded to Vendor 2") { id }
}

# Flag a log entry as a compliance violation
mutation { flagViolation(logId: 1) { id isViolation } }

# Get the full compliance report (all violations)
query { getComplianceReport { id eventType entityType details isViolation } }
```

---

## GraphQL API Reference

### Queries
| Query | Role Required | Description |
|---|---|---|
| `getTenders` | Any | List all active tenders |
| `getBidsByTender(tenderId)` | Any | List bids for a tender |
| `compareBids(tenderId)` | ADMIN, OFFICER | Rank bids by final score |
| `getEvaluationResults(bidId)` | ADMIN, EVALUATOR | View all evaluator scores |
| `detectBidCollusion(tenderId)` | ADMIN, AUDITOR | Fraud: collusion check |
| `analyzeVendorPatterns` | ADMIN, AUDITOR | Fraud: cartel pattern check |
| `getFraudAlerts(tenderId)` | ADMIN, AUDITOR | Fraud: price anomaly check |
| `getPendingRoleRequests` | ADMIN | View pending role upgrade requests |
| `getPendingVendors` | ADMIN | View vendors awaiting KYC approval |

### Mutations
| Mutation | Role Required | Description |
|---|---|---|
| `register` / `login` | Public | Authentication |
| `createTender` | PROCUREMENT_OFFICER | Create new tender |
| `submitBid` | VENDOR | Submit bid (blocked if blocklisted) |
| `assignEvaluator` | OFFICER | Assign evaluator to a bid |
| `evaluateTechnicalBid` | EVALUATOR | Submit technical score |
| `evaluateFinancialBid` | EVALUATOR | Submit financial score |
| `finalizeEvaluation` | OFFICER | Calculate weighted final score |
| `overrideEvaluation` | ADMIN | Admin override with justification |
| `blockUser` | ADMIN | Add vendor to blocklist |
| `unblockUser` | ADMIN | Remove vendor from blocklist |
| `approveVendor` | ADMIN | Approve vendor KYC |
| `awardContract` | ADMIN | Award contract to vendor |
| `signContract` | VENDOR | Digitally sign contract |
| `activateContract` | ADMIN | Activate signed contract |
| `releasePayment` | FINANCE | Release milestone payment |
| `processPenalty` | FINANCE | Apply financial penalty |
| `flagViolation` | AUDITOR | Flag an audit log violation |

---

## Running Tests

Tests are written using **JUnit 5** and **Mockito** and are located in `src/test/java` inside each module.

```bash
# Run all tests
mvn test

# Run tests for a specific module
mvn test -pl bid-management
mvn test -pl fraud-management
mvn test -pl vendor-management
mvn test -pl contract-financial
mvn test -pl compliance-audit
```

### Test Coverage Summary
| Module | Test File | Cases |
|---|---|---|
| `bid-management` | `BidServiceTest` | Submit bid, blocklist enforcement, conflict of interest, weighted score calculation |
| `fraud-management` | `FraudDetectionServiceTest` | Collusion, cartel patterns, price anomalies, block/unblock user |
| `vendor-management` | `VendorServiceTest` | Registration, duplicate check, approve/reject |
| `contract-financial` | `ContractServiceTest` | Full contract lifecycle, payment scheduling, penalty, refund |
| `compliance-audit` | `AuditServiceTest` | Event logging, violation flagging |
| `tender-management` | `TenderServiceTest` | Tender creation and status transitions |
| `auction-engine` | `AuctionServiceTest` | Auction start, place bid, leaderboard |

---

## Error Handling

All errors are handled globally by `GlobalGraphQLExceptionHandler` and returned as clean GraphQL errors.

| Exception Type | GraphQL Error Type | HTTP Meaning |
|---|---|---|
| `ResourceNotFoundException` | `NOT_FOUND` | 404 — Entity doesn't exist |
| `UnauthorizedException` | `UNAUTHORIZED` | 401 — Access denied |
| `IllegalArgumentException` | `BAD_REQUEST` | 400 — Invalid input |
| `IllegalStateException` | `BAD_REQUEST` | 400 — Invalid state transition |
| Any other `RuntimeException` | `INTERNAL_ERROR` | 500 — Unexpected error |

**Example error response:**
```json
{
  "errors": [{
    "message": "Contract must be in GENERATED status to sign",
    "locations": [{ "line": 2, "column": 3 }],
    "path": ["signContract"],
    "extensions": {
      "classification": "BAD_REQUEST"
    }
  }],
  "data": null
}
```
