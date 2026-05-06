# Coding Convention

## Objective

This document defines the mandatory coding conventions for Backend `Spring Boot` and Frontend `React` so that the codebase stays readable, maintainable, reviewable, and aligned with the architecture, API, and database documents.

## General Principles

- Variable, function, and class names should be more meaningful than long comments.
- Every function, class, and component must have a clear responsibility.
- Do not duplicate business rules in multiple places.
- Every new change must respect the agreed architecture, API contracts, and database rules.
- New code must include tests, or have a clear reason if it cannot be tested yet.
- Pull requests should be small enough to review easily and roll back safely if needed.

## Backend (Spring Boot 3.5 + Java 17)

### Mandatory naming rules

- Classes use `PascalCase`.
- Methods, fields, and local variables use `camelCase`.
- Constants use `UPPER_SNAKE_CASE`.
- Packages use lowercase letters and are organized by business module.
- Class names must reflect their actual role, for example `OrderController`, `OrderApplicationService`, `OrderRepositoryImpl`, `CustomerFacade`.
- Method names must start with a verb and describe behavior accurately.

### Mandatory code structure by module

The backend must follow `modular architecture`, and each module must contain `api`, `application`, `domain`, `infrastructure`.

- HTTP code belongs in `api`.
- Use cases, orchestration, and transaction boundaries belong in `application`.
- Core business rules, value objects, and repository contracts belong in `domain`.
- JPA repository implementations, external clients, messaging, and batch adapters belong in `infrastructure`.
- Do not create services, repositories, or utilities that blur module boundaries.

### Mandatory Java code rules

- Controllers may only handle HTTP concerns such as request mapping, validation, and response status.
- Application services must contain orchestration, transactions, permission checks, and required dependency calls.
- The domain must not depend on the Spring framework, HTTP clients, database implementations, or controller DTOs.
- Repositories are only responsible for data access.
- DTOs, entities, domain models, and response models must not be used interchangeably.
- Constructor injection is mandatory. Field injection is not allowed.
- Do not create generic utility classes that bundle business rules without a clear business boundary.

### Rules for communication between modules

- Module A may call module B only through an `application facade` or another explicitly exposed interface.
- Do not call another module's `infrastructure` or `domain internals` directly.
- Do not import another module's request DTOs, response DTOs, or internal entities for reuse.
- Data exchanged between modules must go through a clear contract.
- Asynchronous business flows between modules must go through clearly designed events or messages.

### Mandatory Spring conventions

- `@Transactional` may only be placed in the `application` layer or in facades that act as transaction boundaries.
- Input validation must be defined in request DTOs using `jakarta.validation`.
- HTTP exceptions must be mapped centrally through `@RestControllerAdvice`.
- Config classes must be separated by concern such as `security`, `web`, `datasource`, `messaging`, `batch`.
- Bean configuration must stay close to the related concern or module, not grouped into a large unfocused configuration area.

### Mandatory JPA, entity, and database conventions

- Entities may be used to map the database's existing schema.
- Entities must map tables, columns, primary keys, foreign keys, and nullability exactly according to the real schema.
- Do not write code that relies on Hibernate to create or modify the schema automatically.
- `spring.jpa.hibernate.ddl-auto` must be disabled.
- Do not use `create`, `create-drop`, `update`, or any mode that generates DDL.
- Do not commit migrations or code that adds, changes, or deletes the existing schema without official approval.
- Repository implementations belong in `infrastructure`; repository contracts belong in `domain`.

### Mandatory API coding conventions

- Controllers must use request DTOs and response DTOs. Do not expose entities directly through APIs.
- JSON fields must use `camelCase`.
- Datetime values returned by APIs must follow `ISO-8601 UTC` with suffix `Z`.
- API errors must follow the shared format defined in `02-api-guideline.md`.
- Controllers must not parse business rules or work with repositories directly.

### Mandatory logging rules

- Use log levels correctly: `ERROR`, `WARN`, `INFO`, `DEBUG`.
- Do not log passwords, tokens, OTPs, secrets, or raw sensitive information.
- Logs should include context such as `traceId`, `requestId`, `userId`, `resourceId` when available.
- Business and system errors must be easy to debug without exposing internal details to clients.

### Mandatory exception handling rules

- Do not throw generic `Exception` or `RuntimeException` for business cases.
- Important business cases must have clearly named custom exceptions.
- Exception-to-HTTP-response mapping must go through centralized handlers.
- Internal exception messages must not be shown verbatim to end users.

### Mandatory Spring Batch conventions

- `Spring Batch` must follow modular architecture.
- `reader`, `processor`, `writer`, and `listener` belong in `infrastructure`.
- Business rules inside batch flows belong in `domain` or `application`; do not hide core business logic in `reader`, `processor`, or `writer`.
- Batch jobs must not call repository implementations of other modules directly.
- Batch jobs should reuse `application facades` when they share business flows with APIs.

### Mandatory Backend testing rules

- Business rules in `domain` must have unit tests.
- `application` services must have tests for main use cases, business validation, transaction flow, and important error cases.
- Repositories, security, and API contracts must have integration tests when related changes are made.
- Batch jobs, batch steps, and idempotency must have tests when batch behavior changes.
- Test names must describe behavior, for example `shouldReturn409WhenOrderAlreadyClosed`.

## Frontend (React + Vite + TypeScript + Tailwind CSS)

### Mandatory general principles

- The frontend must use `React`, `Vite`, and `TypeScript`.
- Frontend code must follow `feature-based architecture`.
- Do not add plain JavaScript files in `src`, except tool config files when necessary.
- Components, hooks, services, and types must stay within the correct feature boundary or shared app-level boundary.

### Mandatory naming rules

- Component files use `PascalCase`, for example `UserTable.tsx`.
- Hooks use the `use` prefix, for example `useUserList.ts`.
- Utility files use `kebab-case` and must be consistent across the project.
- Type files use `kebab-case`, for example `order-create-command.ts`, `user-summary.ts`.
- CSS modules, test files, and story files, if any, should be named close to the related component.

### Mandatory feature-based code structure

- `pages` may only compose pages and route-level layouts.
- `features` must contain code organized by business use case.
- `components` may only contain reusable shared UI components.
- `shared` may only contain constants, utilities, tokens, base UI, and generic types.
- `services` may only contain the shared API client, interceptors, transport wrappers, or app-level integrations.
- `hooks` may only contain shared app-level custom hooks; business hooks belong inside features.
- `config` may only contain app-level frontend configuration.
- `routes` may only contain route declarations, route guards, and route metadata.
- `locales` may only contain i18n resources and locale configuration.
- `layouts` may only contain page-level or app-level layouts.
- Each feature must encapsulate its own UI, hooks, services, and types.
- Each feature should expose a clear entry point through `index.ts` when needed by a page or another feature.

### Mandatory component rules

- Page components may only compose UI and invoke the required features.
- Components must not hold too many responsibilities.
- Props must be clear, minimal, and unambiguous.
- Do not pass too many boolean flags to control multiple modes in one large component.
- Prefer composition instead of rapidly increasing `if`, `switch`, or `variant` complexity inside a component.
- Complex logic should be extracted from JSX into hooks, functions, or child components.

### Mandatory state and effect rules

- Forms with validation, submit flow, or API mapping must use `React Hook Form`.
- Use local state for component-internal state that serves only that component.
- Do not put server state into a global UI store if it is already properly managed by the API/query layer.
- Use `useEffect` only for real side effects.
- Do not use `useEffect` to compute data that can be derived directly during render.
- Complex effect logic should be extracted into custom hooks.

### Mandatory API and adapter rules

- Do not call `fetch` or `axios` directly inside components.
- Every request must go through an `api client` or service layer.
- Components must not parse raw API responses.
- Backend DTOs must be mapped into UI models at the adapter or service layer.
- The frontend must fully handle `loading`, `empty`, and `error` states.
- Mapping backend `error.code` values to UI messages must be consistent across the whole app.

### Mandatory style and UI rules

- The frontend must use `Tailwind CSS` consistently across the codebase.
- Colors, spacing, and typography must come from design tokens or shared conventions.
- Do not arbitrarily hard-code colors, spacing, or sizes when matching tokens already exist.
- Do not hard-code text if the screen falls within an i18n requirement.

### Mandatory Frontend testing rules

- Utilities, adapters, and hooks with business mapping must have unit tests.
- Components with important interactions must have component tests.
- Main business flows must have E2E tests when they are part of important releases.
- When fixing a bug, add a test to prevent recurrence whenever the issue can be reproduced in a test.

## Coding Convention Review Checklist

- Are variable, function, class, and component names meaningful?
- Does the code respect the Backend boundaries of `api`, `application`, `domain`, `infrastructure`?
- Does the code respect the Frontend boundaries of `pages`, `features`, `components`, `shared`, `services`, `hooks`, `config`, `routes`, `locales`, `layouts`?
- Does anything violate API contracts, error format, datetime format, or naming conventions?
- Does anything violate database rules such as `ddl-auto`, schema migrations, or incorrect entity mapping?
- Are business rules duplicated or placed in the wrong layer?
- Are logging, exception handling, and validation consistent?
- Do tests cover the main business paths and the important bug risks?
