# Project Backend

Backend skeleton built with `Spring Boot 3.5` and `Java 17`, organized by `modular architecture`. The repository is intended as a reference project for structuring modules, separating layers, and standardizing API, persistence, and batch patterns across the team.

## Overview

- Project name: `project-backend`
- Language: `Java 17`
- Framework: `Spring Boot 3.5.0`
- Build tool: `Maven 3.9.x`
- Database: `MariaDB`
- Persistence: `Spring Data JPA`
- Batch support: `Spring Batch`
- API documentation: `Springdoc OpenAPI` / Swagger UI

Current business modules:

- `customer`: query customer information and expose customer data to other modules through a facade.
- `order`: create and query orders while validating customer status through the `customer` module.
- `batch`: sample batch entry point for internal job execution.

Shared support modules:

- `common`: API exception and error response handling.
- `config`: application-wide configuration such as OpenAPI.

## Architecture

The codebase follows `modular architecture` at the package level. Each module applies `layered architecture` internally:

- `api`: controllers, request/response DTOs, validation, API mappers
- `application`: use cases, orchestration, transaction boundaries, facades
- `domain`: business models, rules, repository contracts
- `infrastructure`: JPA entities, Spring Data repositories, contract implementations

Dependency direction:

```text
api -> application -> domain
infrastructure -> domain
```

Rules used in this repository:

- `domain` must not depend on web, Spring MVC, or controller DTOs.
- `infrastructure` implements domain contracts instead of leaking persistence details upward.
- cross-module access goes through `application facade`, not another module's repository or infrastructure classes.

Example:

- `order` depends on `customer.application.CustomerFacade`
- `order` does not call `customer.infrastructure.persistence.CustomerJpaRepository`

## Technology Stack

Main dependencies declared in [pom.xml](pom.xml):

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-batch`
- `springdoc-openapi-starter-webmvc-ui`
- `mariadb-java-client`

## Project Structure

```text
.
|- pom.xml
|- src/
|  |- main/
|  |  |- java/com/company/project/
|  |  |  |- ProjectBackendApplication.java
|  |  |  |- config/
|  |  |  |- common/
|  |  |  |- customer/
|  |  |  |- order/
|  |  |  `- batch/
|  |  `- resources/
|  `- test/
|- docs/
`- docs-vi/
```

Key locations:

- [src/main/java/com/company/project/ProjectBackendApplication.java](src/main/java/com/company/project/ProjectBackendApplication.java): Spring Boot application entry point
- [src/main/java/com/company/project/config/OpenApiConfig.java](src/main/java/com/company/project/config/OpenApiConfig.java): OpenAPI and Swagger metadata
- [src/main/resources/application.yml](src/main/resources/application.yml): shared application configuration and default profile
- [src/test/java](src/test/java): unit and integration test source

## Module Guide

### Customer Module

Package: `com.company.project.customer`

Responsibility:

- read customer data
- expose a public facade for other modules
- provide customer API response mapping

Important files:

- [CustomerController.java](src/main/java/com/company/project/customer/api/CustomerController.java): `GET /api/v1/customers/{customerId}`
- [CustomerQueryService.java](src/main/java/com/company/project/customer/application/CustomerQueryService.java): query use case for customer details
- [CustomerFacade.java](src/main/java/com/company/project/customer/application/CustomerFacade.java): contract used by other modules
- [CustomerFacadeImpl.java](src/main/java/com/company/project/customer/application/CustomerFacadeImpl.java): implementation of cross-module access
- [CustomerRepository.java](src/main/java/com/company/project/customer/domain/repository/CustomerRepository.java): domain repository contract
- [CustomerRepositoryImpl.java](src/main/java/com/company/project/customer/infrastructure/persistence/CustomerRepositoryImpl.java): JPA-backed repository implementation

### Order Module

Package: `com.company.project.order`

Responsibility:

- accept order creation requests
- expose order detail and list APIs
- validate that the customer is active
- persist order aggregate and order items

Important files:

- [OrderController.java](src/main/java/com/company/project/order/api/OrderController.java): `GET /api/v1/orders`, `GET /api/v1/orders/{orderId}`, `POST /api/v1/orders`
- [OrderResponseMapper.java](src/main/java/com/company/project/order/api/mapper/OrderResponseMapper.java): maps application read models to API responses
- [OrderApplicationService.java](src/main/java/com/company/project/order/application/OrderApplicationService.java): main order creation flow
- [OrderQueryService.java](src/main/java/com/company/project/order/application/OrderQueryService.java): read-side service for order detail and paged order list
- [OrderView.java](src/main/java/com/company/project/order/application/OrderView.java): application view model for order detail/list responses
- [Order.java](src/main/java/com/company/project/order/domain/model/Order.java): order aggregate with creation rules
- [OrderRepository.java](src/main/java/com/company/project/order/domain/repository/OrderRepository.java): domain persistence contract
- [OrderRepositoryImpl.java](src/main/java/com/company/project/order/infrastructure/persistence/OrderRepositoryImpl.java): maps domain aggregate to JPA persistence
- [OrderJpaEntity.java](src/main/java/com/company/project/order/infrastructure/persistence/OrderJpaEntity.java): JPA entity for `orders`

### Batch Module

Package: `com.company.project.batch`

Responsibility:

- expose a sample batch trigger endpoint
- centralize batch execution rule checks
- provide a place for future scheduler and job runner expansion

Important files:

- [BatchJobController.java](src/main/java/com/company/project/batch/api/BatchJobController.java): `POST /api/v1/batch-jobs/customer-sync`
- [CustomerSyncJobFacade.java](src/main/java/com/company/project/batch/application/CustomerSyncJobFacade.java): batch trigger orchestration
- [BatchExecutionRule.java](src/main/java/com/company/project/batch/domain/BatchExecutionRule.java): placeholder for job execution policy
- [CustomerSyncJobRunner.java](src/main/java/com/company/project/batch/infrastructure/job/CustomerSyncJobRunner.java): sample runner implementation

### Common Module

Package: `com.company.project.common`

Responsibility:

- define shared business exception model
- standardize API error format
- convert validation and runtime exceptions into HTTP responses

Important files:

- [BusinessException.java](src/main/java/com/company/project/common/api/BusinessException.java)
- [ApiErrorResponse.java](src/main/java/com/company/project/common/api/ApiErrorResponse.java)
- [ApiExceptionHandler.java](src/main/java/com/company/project/common/api/ApiExceptionHandler.java)

## Configuration

Application configuration is profile-based and lives in `src/main/resources`.

- [application.yml](src/main/resources/application.yml): base config, default profile, JPA and Springdoc settings
- [application-local.yml](src/main/resources/application-local.yml): local datasource
- [application-develop.yml](src/main/resources/application-develop.yml): develop datasource
- [application-prod.yml](src/main/resources/application-prod.yml): production datasource

Default profile:

- `local`

Database connection is configured through Spring Boot auto-configuration. There is no dedicated `DatabaseConfig.java` in this repository because the datasource is created from `spring.datasource.*` properties in the YAML files.

## Running the Application

### Prerequisites

- `Maven 3.9.x`
- `JDK 17`
- running `MariaDB` instance

### Commands

```bash
mvn spring-boot:run
mvn spring-boot:run -Plocal
mvn spring-boot:run -Pdevelop
mvn spring-boot:run -Pprod
mvn test
mvn clean package
```

### Active profile

By default, the application uses the `local` profile from [application.yml](src/main/resources/application.yml).

Environment-specific run commands:

```bash
mvn spring-boot:run -Plocal
mvn spring-boot:run -Pdevelop
mvn spring-boot:run -Pprod
```

Behavior:

- `mvn spring-boot:run`: runs with the default profile configured in `application.yml`
- `mvn spring-boot:run -Plocal`: runs with `spring.profiles.active=local`
- `mvn spring-boot:run -Pdevelop`: runs with `spring.profiles.active=develop`
- `mvn spring-boot:run -Pprod`: runs with `spring.profiles.active=prod`

## API Endpoints

The repository currently exposes these sample endpoints:

- `GET /api/v1/customers`
- `GET /api/v1/customers/{customerId}`
- `GET /api/v1/orders`
- `GET /api/v1/orders/{orderId}`
- `POST /api/v1/orders`
- `POST /api/v1/batch-jobs/customer-sync`

## Response Convention

For frontend multi-language support, the backend returns stable `code` values and keeps `message` as a fallback/debug field.

Recommended frontend behavior:

- use `code` as the primary key for i18n translation
- treat `message` as fallback text, not the final UI copy
- use validation `details[].code` for field-level translations

Examples:

Business error:

```json
{
  "code": "CUSTOMER_NOT_FOUND",
  "message": "Customer not found"
}
```

Validation error:

```json
{
  "code": "VALIDATION_ERROR",
  "details": [
    {
      "field": "email",
      "code": "EMAIL",
      "message": "must be a well-formed email address"
    }
  ]
}
```

Success response:

```json
{
  "id": 1001,
  "code": "ORDER_CREATED"
}
```

Swagger resources:

- API docs: `/api-docs`
- Swagger UI: `/swagger-ui.html`
- default server port: `8081`

## Testing

Current test source is located in [src/test/java](src/test/java).

Example:

- [OrderTest.java](src/test/java/com/company/project/order/domain/model/OrderTest.java): verifies domain rules for order creation
- [OrderApplicationServiceTest.java](src/test/java/com/company/project/order/application/OrderApplicationServiceTest.java): verifies active-customer validation and order creation flow

Run all tests with:

```bash
mvn test
```

## Documentation

Architecture and engineering guidelines are versioned in the repository:

- [docs/01-architecture.md](docs/01-architecture.md)
- [docs/02-api-guideline.md](docs/02-api-guideline.md)
- [docs/03-database-guideline.md](docs/03-database-guideline.md)
- [docs/04-coding-convention.md](docs/04-coding-convention.md)
- [docs/05-git-rule.md](docs/05-git-rule.md)
- [docs/06-deployment.md](docs/06-deployment.md)
- [docs/07-troubleshooting.md](docs/07-troubleshooting.md)
- [docs/08-testing-guideline.md](docs/08-testing-guideline.md)

Vietnamese versions are available in [docs-vi](docs-vi).

## Notes

- `target/` contains generated artifacts and reports, not hand-maintained source code.
- the repository is a skeleton project, so some modules intentionally contain placeholder implementations to demonstrate structure rather than full business behavior.
- when adding a new module, keep the same `api`, `application`, `domain`, `infrastructure` split and expose cross-module access through an application facade.
