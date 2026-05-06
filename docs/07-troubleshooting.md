# Troubleshooting Guide

## Objective

This document collects common approaches for handling issues during development, deployment, and operations.

## General Principles

- Reproduce the issue before fixing it.
- Prioritize evidence gathering: logs, metrics, traces, screenshots, request payloads.
- Clearly separate whether the problem belongs to backend, frontend, or the contract between them.
- Record the root cause and solution to avoid repeated incidents.

## Incident handling process

1. Identify the impact scope.
2. Check the latest changes: code, config, customer-provided schema, data.
3. Collect logs and traces using `traceId` when available.
4. Reproduce the issue on `local`, `develop`, or the affected environment.
5. Narrow it down by layer: FE, API, DB, external service.
6. Fix the issue, add tests, and update documentation if needed.

## Backend (Spring Boot)

### Application fails to start

- Check boot logs.
- Verify environment variables, active profile, port, and datasource.
- Check whether customer-provided schema changes or datasource configuration have drifted.
- Check bean configuration, circular dependencies, and classpath issues.

### Database connection error

- Check the URL, username, password, and network access.
- Confirm that the customer-provided schema exists, is at the correct version, and matches the current entity mappings.
- Check whether the connection pool is exhausted.

### Slow API response

- Check SQL logs, N+1 queries, and external API latency.
- Review CPU, memory, thread pool, and DB slow query metrics.
- Check whether transactions are being held open too long.

### `4xx` or `5xx` errors

- `400/422`: check payload, validation, and business rules.
- `401/403`: check auth token, roles, and permission mapping.
- `500`: compare the exception stack trace with the request, traceId, and external dependencies.

## Frontend (React)

### Application fails to build

- Check dependencies, environment variables, import paths, and type errors.
- Check for version differences between local and CI Node.js environments.
- If the error appears after an API contract change, verify adapters and types.

### UI does not display data

- Check the request in the browser network tab.
- Verify query keys, cache state, loading state, and error state.
- Check whether the adapter maps API fields correctly.

### CORS or auth errors

- Verify that the frontend origin is allowed by the backend.
- Check whether the token is sent in the correct header.
- Check the refresh token flow or login redirect behavior.

### Unstable UI behavior

- Check for race conditions between multiple requests.
- Check the dependency array of `useEffect`.
- Verify whether a component is keeping stale state after route changes.

## BE and FE communication issues

### Contract mismatch

- Compare the real payload with OpenAPI or the mock contract.
- Check required fields, enums, date format, and pagination.
- If the backend changed the contract, confirm whether versioning or feature flags exist.

### Issue happens only in production

- Check configuration differences between environments.
- Verify that the frontend build uses the correct API base URL.
- Check secrets, CORS, CDN cache, and whether customer-side schema changes were fully applied.

## Post-incident follow-up

- Add tests for the issue that occurred.
- Update documentation or alerts if needed.
- Create a postmortem for serious incidents.
