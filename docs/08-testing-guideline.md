# Testing Guideline

## Objective

This document defines the mandatory testing strategy for Backend `Spring Boot` and Frontend `React` to ensure business correctness, refactor safety, and lower regression risk.

## General Principles

- Testing is a required part of code changes, not an afterthought.
- Every business change, contract change, or bug fix must be evaluated for additional testing needs.
- Tests must protect meaningful behavior, not just increase coverage.
- Tests must be stable, repeatable, and independent from random execution order.
- When a test fails, it should point clearly to the root cause for easier debugging.

## Definition of test types

### Unit test

- A unit test validates a small unit of code such as a function, class, hook, adapter, or domain service.
- Unit tests must run fast, independently, and without depending on a real database, real network, or a large framework context.
- Unit tests protect business rules, utility logic, mapping logic, and small edge cases.

### Integration test

- An integration test validates how multiple real components work together.
- Integration tests are used for repositories, API contracts, security, serialization, database queries, transaction flows, message integration, and batch flows.
- Integration tests may use Spring context, a test database, or Testcontainers when needed.

### Component test

- A component test validates the behavior of a React component in a realistic render context.
- Component tests focus on interactions, state changes, loading states, empty states, error states, and rendering based on input data.

### End-to-End test

- An E2E test validates a main user flow from start to finish on an application that is close to the real environment.
- E2E tests protect critical business flows, high-risk releases, and FE/BE cross-integration.

## Backend (Spring Boot 3.5 + Java 17 + Spring Batch)

### Mandatory test rules by layer

- `domain` must be protected by unit tests.
- `application` must have tests for main use cases, business validation, transaction flows, and important error cases.
- `infrastructure` must have integration tests when repositories, queries, external integrations, message flows, or persistence behavior change.
- `api` must have controller tests or API integration tests when endpoints, request DTOs, response DTOs, validation, error format, auth, or serialization change.
- `batch` must have dedicated tests for jobs, steps, idempotency, retry, skip, and mapping logic when related behavior changes.

### When unit tests are mandatory for Backend

- Add or modify business rules in `domain`.
- Add or modify an `application service` with decision logic.
- Add or modify utilities or mappers that contain business handling.
- Fix a bug that can be reproduced at function, class, or small service level.

### When integration tests are mandatory for Backend

- Add or modify repositories, custom queries, native queries, or JPA mappings.
- Add or modify API endpoints.
- Add or modify security rules, auth flow, or permission checks.
- Add or modify transaction boundaries, persistence behavior, lazy/eager loading, or lock strategy.
- Add or modify batch jobs, batch steps, readers, writers, or processors.
- Add or modify an external client that the application depends on directly.

### Rules for database tests

- Do not enable `ddl-auto` in tests to auto-generate schemas against project rules.
- Database-related integration tests must follow the real schema or a test setup equivalent to the real schema.
- Important query tests must validate correct results, correct filters, correct sorting, and limit N+1 risk when applicable.
- When entity mappings change, tests must protect column names, nullability, and key relationships.

### Rules for API tests

- API tests must validate status codes, response bodies, error format, validation, and datetime format when related behavior changes.
- API tests must not only check `200 OK`; they must also include error cases when the endpoint has validation or authorization behavior.
- If the API contract changes, tests must protect the new contract and must not ignore backward compatibility risks.

### Rules for batch tests

- Batch tests must protect per-record processing logic, overall job results, retry/skip behavior, and idempotency.
- If the batch reads/writes the database, tests must include edge data and invalid data cases.
- Batch tests must not only check that the job runs successfully; they must include important business failure cases when applicable.

## Frontend (React + Vite + TypeScript + Tailwind CSS)

### Mandatory test rules by code type

- `util`, `adapter`, `formatter`, `mapper`, and `custom hook` code with business logic must have unit tests.
- React components with important interactions must have component tests.
- Features with loading, empty, error, submit flow, or important state transitions must have protective tests.
- Main business flows or high release-risk areas must have E2E tests.

### When unit tests are mandatory for Frontend

- Add or modify a hook with business logic.
- Add or modify an adapter that maps DTOs to UI models.
- Add or modify a utility that handles dates, enums, money, validation, filtering, or transforms.
- Fix a bug that can be reproduced at function, hook, or adapter level.

### When component tests are mandatory for Frontend

- Add or modify a form, modal, table, filter, pagination component, or another stateful widget.
- Add or modify a component with loading, empty, or error states.
- Add or modify interactions such as click, submit, input change, retry, toggle, or select.
- Add or modify a component whose rendered state affects the business flow.

### When E2E tests are mandatory

- Add or modify key user flows such as login, create, update, delete, submit data, payment, or batch-trigger synchronization if there is a UI for it.
- Fix a bug that previously happened in a near-production environment and is not sufficiently protected by component tests.
- Release changes with high risk because they affect FE, BE, and the contract at the same time.

### Rules for API mocking in Frontend

- Unit tests and component tests may mock APIs to protect component behavior.
- Do not make component tests depend entirely on a real backend when the goal is only to validate UI behavior.
- Mock responses must follow the real API contract.
- If the API contract changes, related mock tests must be updated at the same time.

## Rules for bug fixes

- Every bug fix must be evaluated for whether it can be reproduced by a test.
- If the bug can be reproduced by a test, the team must add a regression test before or together with the fix.
- If a test cannot be added yet, the PR must clearly explain the reason and the remaining risk.

## Rules for test data

- Test data must be meaningful, easy to read, and reflect the business correctly.
- Do not hard-code confusing test data or rely on mutating shared state across multiple tests.
- Each test must have clear setup/teardown when shared resources are used.
- Tests must not depend on data that already exists in external environments.

## Rules for test naming

- Test names must describe the behavior being protected.
- Backend tests should follow the `should...When...` style.
- Frontend tests should follow user behavior or expected render outcomes.

Examples:

- `shouldReturn409WhenOrderAlreadyClosed`
- `shouldSaveOrderWhenInputIsValid`
- `shouldShowErrorMessageWhenLoginFails`
- `shouldDisableSubmitButtonWhileSubmitting`

## Rules for running tests in the pipeline

- Pull requests must run the minimum relevant test suite for the change scope.
- Merges into main branches must have passing CI results.
- Deploy builds to environments must include tests and smoke checks as defined in `06-deployment.md`.
- Failing tests in CI must not be ignored without approval and a clear reason.

## Test Review Checklist

- Does this change require additional unit tests?
- Does this change require additional integration tests?
- Does this bug fix already have a regression test?
- Do the tests protect both happy paths and unhappy paths?
- Are the tests readable, stable, and not flaky?
- Do mocks follow the real contract?
- Do integration tests truly validate the persistence, API, security, or flow they are meant to protect?
- Are loading, empty, error, or permission cases missing?
- Do the new tests reflect the ticket goal and the risk of the change?
