# Payment Gateway API

Version 1 Business Requirements

Document revision 0.1 | 11 October 2026 | Proposed baseline for owner review

Project owner: Julius Parco  
Product milestone: V1 payment intent foundation  
Release status: Implemented in an open pull request; review remains in progress

## Purpose and business outcome

Build a reproducible Java backend portfolio demonstration that Julius can run, test and explain in interviews. Version 1 demonstrates an authenticated payment intent lifecycle backed by PostgreSQL, with clear validation, ownership rules and repeatable confirmation.

This is a personal learning and portfolio project. Payments are simulated. The application does not move money, communicate with card networks or represent paid client work.

### Who uses this document

Julius uses this baseline to decide what belongs in V1 and when it is complete. Reviewers use the requirement IDs and evidence to assess behaviour. Interviewers can use the scope and demonstration criteria to understand what the project proves.

### Business objectives

- Restore hands-on Java and Spring development skills through work Julius can explain and maintain.

- Provide verifiable evidence through source code, focused commits, automated checks and a repeatable local demonstration.

- Establish a small, tested foundation for later event delivery and reliability milestones.

### Definition of success

An authenticated demo client can create, retrieve and confirm its own payment intent. Repeated sequential confirmation preserves the successful outcome. Unauthenticated or unauthorized operations are rejected. The documented setup works locally, tests pass, review findings are resolved, and Julius explicitly approves release through the pull request.

V1 is not yet accepted. A passing build is evidence toward acceptance, not a substitute for the remaining security review and tests.

## Version 1 scope and workflow

| Included in V1 | Excluded from V1 |
| --- | --- |
| Demo client credentials and short-lived JWTs | Production identity management and real payment credentials |
| Create, retrieve and confirm owned payment intents | Real authorization, settlement, refunds or card storage |
| PostgreSQL persistence and Flyway migrations | Kafka, transactional outbox and Redis request idempotency |
| Validation, API errors, unit and integration tests | Notification, Order, Analytics and Ledger services |
| Docker setup, GitHub Actions and runnable documentation | Production deployment, load targets and operational guarantees |

### Primary user journey

1. The demo client submits valid credentials and receives a JWT access token.

2. The client creates a payment intent for 2500 minor units in CAD, representing CAD 25.00.

3. The API saves a UUID, amount, currency, optional description, owner, status and timestamps.

4. The client retrieves the intent in REQUIRES_CONFIRMATION state.

5. The client confirms the intent; the simulated result becomes SUCCEEDED.

6. The client repeats confirmation and retrieves the same successful payment.

### Payment state model

Create  →  REQUIRES_CONFIRMATION  →  Confirm  →  SUCCEEDED  
Confirm again  →  SUCCEEDED with no new payment or status transition

### Business rules

- Ownership comes from the authenticated token subject, not a client-supplied owner field. A client must not read or confirm another client’s payment.

- Amounts are positive integers in currency minor units. V1 demonstrates CAD; it performs no currency conversion.

- Currency input is currently checked for three uppercase letters. This is format validation, not validation against an authoritative currency catalogue.

- Description is optional and limited to 255 characters. Payment IDs are UUIDs; timestamps record creation and the last state change.

- Sequential confirmation retries preserve the result. Safe retries of payment creation and concurrent confirmation guarantees are outside the demonstrated V1 behaviour.

## Functional requirements and acceptance

These criteria define the V1 target. The evidence column distinguishes demonstrated behaviour from checks still required before acceptance.

| ID | Requirement and acceptance criterion | Evidence at 11 October |
| --- | --- | --- |
| FR01 | Valid demo credentials return a bearer token with a limited lifetime. Incorrect credentials return 401. | Valid login passed manually and in tests. Incorrect-credential test pending. |
| FR02 | Protected payment endpoints reject missing, malformed, expired or incorrectly signed tokens with 401 and do not change payment data. | Missing-token creation test passed. Other negative token tests pending. |
| FR03 | A valid create request returns 201, a Location header and a unique payment in REQUIRES_CONFIRMATION. | Automated creation checks and manual CAD 25.00 creation passed. |
| FR04 | The owner can retrieve a payment by ID with 200 and the saved amount, currency, status and timestamps. | Automated and manual retrieval passed. |
| FR05 | The owner can confirm an intent with 200; it becomes SUCCEEDED and updates its timestamp. | Automated and manual confirmation passed. |
| FR06 | Sequential repeated confirmation returns the same successful payment without another state transition. | Automated status checks and manual timestamp comparison passed, allowing database precision differences. |
| FR07 | Another client cannot retrieve or confirm the intent. Return 404 without exposing it; a rejected confirmation leaves it unchanged. | Cross-client retrieval test passed. Cross-client confirmation and unchanged-data assertions pending. |
| FR08 | Invalid amount, currency format or excessive description returns 400 with useful field errors. | Zero amount and lowercase currency tested. Remaining validation boundaries pending. |
| FR09 | Saved payment data remains retrievable after application restart against the same database. | Persistence implemented; explicit post-restart payment retrieval checkpoint pending. |

For missing IDs, the intended response is 404. Broader malformed JSON, invalid UUID and error-response consistency checks remain part of the review. No requirement is marked fully accepted solely because supporting code exists.

## Quality requirements and release gate

| ID | Requirement | Acceptance evidence |
| --- | --- | --- |
| QR01 | Reproducible local setup | Document Java 17, Docker, Maven Wrapper, database port and startup; verify startup using the saved defaults. |
| QR02 | Database integrity | Apply versioned Flyway migrations and validate the JPA schema. Review transaction boundaries and persistence behaviour. |
| QR03 | Security scope | Document demo-only credentials and signing configuration. Store no real card data or personal secrets in Git. Review token and ownership failure paths. |
| QR04 | Automated verification | Maven clean verify succeeds with no skipped tests; security acceptance checks and relevant regression tests pass locally and in CI. |
| QR05 | Maintainability | Keep requirements, README and actual behaviour aligned. Julius can explain controllers, service logic, persistence and security decisions. |

### Release checklist

- Finish review of correctness, security, reliability, maintainability and testing; resolve actionable findings.

- Complete the pending functional acceptance checks, including rejected confirmations that leave data unchanged.

- Run the full local test suite and verify GitHub Actions for the final candidate commit.

- Confirm the documentation supports a fresh local setup and clearly identifies simulated behaviour and limitations.

- Obtain Julius’s explicit merge approval, then merge PR 1. Select any release tag separately; no V1 tag has been established here.

### Current limitations and decisions

Six passing tests provide a useful foundation but do not prove production security, concurrency safety or failure recovery. The generated Spring Security password warning and Testcontainers deprecation notice remain review items, not confirmed defects.

Use host port 5433 for Docker PostgreSQL because an existing Windows PostgreSQL service occupies 5432. The container still uses 5432 internally. DB_URL remains an override for other environments. This choice was verified by successful startup without the override.

Do not add Kafka, Redis or additional services to close V1. New milestones begin on a new branch after the preceding work is accepted. Commits, pushes and merges remain distinct actions; a green build does not authorize a merge.

## Evidence and future versions

### Verified implementation snapshot

Repository: https://github.com/juliusparco16/payment-gateway-api  
Pull request: https://github.com/juliusparco16/payment-gateway-api/pull/1  
Working branch: feature/payment-intent-flow  
Latest pushed commit: 6930783 — fix: avoid local PostgreSQL port conflict  
Local checkout: C:\Java\payment-gateway-api

On 11 October 2026, Maven clean verify completed successfully with 3 service unit tests and 3 integration tests, zero failures, errors or skips. Integration tests use MockMvc with a real temporary PostgreSQL container; they do not start a real HTTP server. Manual PowerShell requests exercised the running HTTP API on port 8080.

Manual evidence: a CAD 25.00 intent moved from REQUIRES_CONFIRMATION to SUCCEEDED, repeated confirmation preserved that state, and a fresh retrieval returned it. Startup after removing DB_URL connected to port 5433 and validated the existing schema. The subsequent GitHub screenshot showed all checks passed and PR 1 remained open.

### Proposed later milestones

| Version | Outcome | Key proof |
| --- | --- | --- |
| V2 | Transactional outbox | Payment change and event record commit together or neither commits. |
| V3 | Kafka delivery and one idempotent consumer | Retry interrupted delivery and handle duplicate events safely. |
| V4 | Request retry protection | Define Redis responsibility, expiration and recovery; prevent duplicate creation for the same request key. |
| V5 | Incremental downstream services | Introduce Notification, Order, Analytics and Ledger one at a time, with reconciliation evidence. |
| V6 | Operational demonstration | Use logs, metrics, failure exercises and measured performance to demonstrate diagnosis and recovery. |

These later versions are proposed planning boundaries, not approved delivery dates or completed features. Each needs its own requirements and acceptance criteria before implementation; testing and documentation continue in every version.

### Document maintenance

Revision 0.1 establishes the initial V1 requirements and status baseline. Update this document after acceptance decisions or scope changes. Preserve requirement IDs so tests and review findings can refer to them. Owner approval of this baseline and final release acceptance are pending.
