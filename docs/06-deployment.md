# Deployment Guideline

## Objective

This document defines how to build, release, deploy, and roll back the Spring Boot backend and React frontend.

## General Principles

- Each environment must have separate configuration.
- Build artifacts must be traceable back to a commit or tag.
- The deployment process must include health checks and a rollback plan.
- Do not manually modify production if the issue can be solved through the pipeline.

## Mandatory environments

- `local`: personal development environment.
- `develop`: internal integration environment, mapped to branch `GDN-dev`.
- `production`: environment serving real users, mapped to branch `GDN-main`.

## Backend (Spring Boot)

### Build artifact

- The backend must be packaged as a `jar` or a container image.
- Artifact versions must be tied to a git commit hash or release tag.
- Runtime configuration must come from environment variables, not hard-coded values.

### Deployment sequence

1. Run tests and static analysis in CI.
2. Build the artifact.
3. Verify database configuration and confirm there are no schema changes that violate project rules.
4. Deploy to the target environment.
5. Check `health`, `readiness`, and API smoke tests.

### Configuration

- Separate `application-local.yml`, `application-develop.yml`, and `application-prod.yml`.
- Store secrets in a secret manager or environment variables.
- Production must have logging and metrics enabled.

### Rollback

- Prefer `roll-forward` if a migration has already run and cannot be safely rolled back.
- If rollback is possible, verify schema and artifact compatibility first.
- Clearly document which migrations are one-way migrations.

## Frontend (React)

### Build artifact

- The frontend must be built into static assets, for example `dist/` or `build/`.
- Asset filenames must include hashes for cache busting.
- Production source maps must be managed securely.

### Configuration

- Environment variables must contain only values that are safe for the client.
- Do not include backend secrets in the frontend build.
- API base URLs and feature flags must be managed per environment.

### Deployment

1. Run lint, unit tests, and build.
2. Publish static assets to hosting/CDN.
3. Invalidate cache if needed.
4. Run smoke tests for critical user flows.

### Rollback

- Roll back by restoring the previous build.
- If the frontend depends on a new API contract, ensure the backend remains compatible with the older FE version during rollback.

## Dependencies between BE and FE

- The backend must not remove old contracts before frontend rollout is complete.
- The frontend must tolerate new backend fields without breaking rendering.
- Releases with two-way impact should be placed behind feature flags when the business allows it.

## Deployment checklist

- Has the correct environment configuration been applied?
- Does the deployment branch match the environment mapping?
  `GDN-dev` -> `develop`, `GDN-main` -> `production`.
- Are health checks and smoke tests ready?
- Is the database migration path safe?
- Are FE and BE contract-compatible?
- Is there a rollback/roll-forward plan?
