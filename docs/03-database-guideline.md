# Database Guideline

## Objective

This document establishes the principles for data design, migrations, and how the frontend should interact with data in a stable way.

## General Principles

- Prioritize relational databases for transactional business flows.
- The system uses an existing database; the team must not add, modify, or delete the existing schema.
- Do not manually edit the schema in database environments.
- Data is a long-term asset, so every change must prioritize backward compatibility.

## Backend (Spring Boot)

### Schema design

- The team must not add new tables, change table structures, change data types, add columns, modify columns, delete columns, add constraints, modify constraints, or delete constraints unless there is separate approval from the database owner.
- The team must not change the schema through migration tools or Hibernate auto DDL on its own.
- Table names use `snake_case`, typically plural for business tables, for example `users`, `orders`.
- Column names use `snake_case`.
- Primary keys should follow one consistent standard, preferably `bigint` or `uuid` depending on the need.
- Include base columns where appropriate: `created_at`, `created_by`, `updated_at`, `updated_by`.
- Use foreign keys when needed to enforce data integrity.

### Normalization and data modeling

- Prefer at least 3NF for transactional systems.
- Only denormalize when there is a clear performance or reporting reason.
- JSON fields in a relational DB should only be used for flexible metadata, not as a replacement for the core business schema.

### Migration

- Within this project, the team must not use `Flyway` or `Liquibase` to create, modify, or delete the existing schema.
- Do not commit migrations that change the schema unless there is an official request and approval.
- If the database owner provides a schema change, the team must update entities, queries, and tests to match the new schema instead of generating schema changes on its own.

### Entity and ORM

- The backend is allowed and encouraged to use entities to map existing tables in the database.
- Entities must map the real table names, column names, primary keys, foreign keys, and nullability constraints exactly as they exist.
- Do not assume Hibernate will create or modify tables automatically to make entities work.
- `spring.jpa.hibernate.ddl-auto` must be disabled. Allowed values are `none` or an equivalent configuration that does not generate DDL.
- Do not use `create`, `create-drop`, `update`, or any configuration that allows Hibernate to change the schema.
- Every new or modified entity must be checked against the real schema before the code is merged.
- If the existing tables do not match common conventions, entities must follow the real schema instead of changing the schema just to make the code look cleaner.

### Index

- Create indexes based on real queries, not intuition.
- Important foreign keys should be considered for indexing.
- Evaluate the trade-off between read speed and write cost.

### Transaction

- Transaction boundaries belong in the service/application layer.
- Keep transactions short and explicit.
- Avoid long network calls inside transactions whenever possible.

### Soft delete and audit

- Use soft delete only when the business requires traceability or recovery.
- If soft delete is used, standardize the column name, for example `deleted_at`.
- Audit logs for sensitive actions should be separated from the main business tables.

### Performance and maintainability

- Do not use `SELECT *`.
- Avoid N+1 queries; monitor them using SQL logs, profilers, or APM.
- Split out read models for reporting if business queries become too complex.

## Frontend (React)

### Client-side data management principles

- The frontend does not own the database schema.
- FE must only work with API contracts and adapted models.
- Do not put logic that depends directly on backend schema details inside components.

### Local storage, session storage, cache

- Store only the minimum necessary data on the client.
- Do not store secrets, passwords, or long-lived access tokens in local storage if safer options exist.
- Clearly define which data is persistent cache and which data is temporary.

### Data format

- Normalize client models when they are rendered in multiple places.
- Date/time must be converted consistently at the adapter layer.
- Enums from the backend must be clearly mapped to UI labels.

### Synchronization with backend

- When the backend adds a new field, FE must treat it as optional until rollout is complete.
- When the backend changes the meaning of a field, FE must not deploy based on assumptions without versioning or a feature flag.

## Database Review Checklist

- Is the schema easy to understand and does it reflect the business correctly?
- Does the code accidentally introduce schema changes through migration or `ddl-auto`?
- Do entities map the real schema correctly?
- Have the main queries been evaluated for indexing?
- Is there risk of N+1, long locks, or dirty writes?
- Is FE coupled too tightly to BE storage details?
