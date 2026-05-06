# API Guideline

## Objective

This document is the mandatory API standard for both the Backend `Spring Boot` team and the Frontend `React` team.

## General Principles

- The API is the shared contract between BE and FE.
- Prioritize simplicity, consistency, and predictability.
- Any change that may break the contract must be reviewed and clearly communicated.
- Request, response, error format, and pagination must be consistent across the whole system.

## Backend (Spring Boot)

### 1. Endpoint naming

- `RESTful resource naming` is mandatory.
- URLs must use `nouns`, not `verbs`.
- Collections use plural nouns: `/users`, `/orders`, `/products`.
- Resource details use `/{resource}/{id}`: `/users/{userId}`.
- Sub-resources should only be used for clear parent-child relationships: `/users/{userId}/addresses`.
- Do not use names such as `/getUsers`, `/createOrder`, `/updateProfile`, `/deleteProduct`.
- Query parameters are only for `filter`, `sort`, `search`, and `pagination`.
- Use action endpoints only when the business action cannot be mapped to CRUD, such as `/orders/{orderId}/cancel`.

Correct:

- `/api/v1/users`
- `/api/v1/users/{userId}`
- `/api/v1/orders/{orderId}/items`
- `/api/v1/orders/{orderId}/cancel`

Incorrect:

- `/api/v1/get-user-list`
- `/api/v1/user/detail/{userId}`
- `/api/v1/getAddressesByUserId/{userId}`
- `/api/v1/cancelOrder/{orderId}`

### 2. HTTP method

- `GET`: retrieve data.
- `POST`: create a new resource or execute a non-idempotent action.
- `PUT`: replace the entire resource.
- `PATCH`: partially update a resource using a partial DTO, not a JSON Patch operation array.
- `DELETE`: delete a resource.

### 3. Request / Response

- DTOs are mandatory; do not expose entities directly.
- JSON fields must use `camelCase`.
- Date/time values must use ISO-8601 and must be in UTC.
- Responses should return only the data needed by the client.
- Do not return internal fields, secrets, or sensitive information.

Required `datetime` JSON format:

- Use `ISO-8601`.
- UTC with suffix `Z` is mandatory.
- Fields containing a full timestamp must use: `yyyy-MM-dd'T'HH:mm:ss'Z'`.
- Do not use ambiguous formats such as `22/06/2026 17:30`, `2026/06/22`, `06-22-2026`, strings without timezone, or datetimes that are not UTC.

Correct:

- `2026-06-22T10:30:00Z`

Incorrect:

- `2026-06-22 10:30:00`
- `22/06/2026 10:30`
- `2026-06-22T10:30:00`
- `2026-06-22T17:30:00+07:00`

Correct `JSON response` examples:

`GET /api/v1/users/{userId}`

```json
{
  "id": 101,
  "name": "Nguyen Van A",
  "email": "a.nguyen@company.com",
  "status": "ACTIVE",
  "createdAt": "2026-06-22T10:30:00Z",
  "updatedAt": "2026-06-22T12:00:00Z"
}
```

`POST /api/v1/users` - `201 Created`

```json
{
  "id": 102,
  "name": "Tran Thi B",
  "email": "b.tran@company.com",
  "status": "ACTIVE",
  "createdAt": "2026-06-22T13:00:00Z",
  "updatedAt": "2026-06-22T13:00:00Z"
}
```

`GET /api/v1/users?page=0&size=20`

```json
{
  "items": [
    {
      "id": 101,
      "name": "Nguyen Van A",
      "email": "a.nguyen@company.com",
      "status": "ACTIVE"
    },
    {
      "id": 102,
      "name": "Tran Thi B",
      "email": "b.tran@company.com",
      "status": "INACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "totalItems": 2,
  "totalPages": 1
}
```

Notes:

- List responses may use a `summary DTO`; they do not need to be 100% identical to detail responses.
- Detail and list responses must use consistent field names when representing the same property.

### 4. Status code

- `200`: success with body.
- `201`: resource created successfully.
- `204`: success with no body.
- `400`: invalid request format or validation failure.
- `401`: unauthenticated.
- `403`: insufficient permission.
- `404`: resource not found.
- `409`: state/data conflict.
- `422`: request format is valid but business validation fails.
- `500`: system error.

### 5. Error format

All API errors must follow one shared format:

```json
{
  "timestamp": "2026-06-22T10:30:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "email",
      "message": "must be a valid email"
    }
  ],
  "traceId": "4fd8b4a2d7e1"
}
```

Mandatory rules:

- `code`: used by FE to map messages.
- `message`: concise and must not expose internal information.
- `details`: optional, only used for validation errors or field-level errors.
- `traceId`: mandatory for debugging.

### 6. Pagination / Filter / Sort

- Standard query parameters: `page`, `size`, `sort`.
- `page` must be `0-based`.
- `size` is the number of records per page.
- `sort` uses the format `field,direction`, for example `createdAt,desc`.
- If there are multiple sort conditions, repeat the `sort` query parameter, for example `?sort=status,asc&sort=createdAt,desc`.
- Filters should be named by business meaning: `status`, `fromDate`, `toDate`.
- Pagination response format must be consistent:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalItems": 0,
  "totalPages": 0
}
```

### 7. Validation / Security / Documentation

- Input validation belongs in request DTOs using `jakarta.validation`.
- Business validation belongs in the service/application layer.
- Use the standard auth header: `Authorization: Bearer <token>`.
- Do not log tokens, passwords, OTPs, or secrets.
- OpenAPI/Swagger is mandatory for every new or changed endpoint.

## Frontend (React)

### 1. API calling approach

- Every request must go through a shared `api client/service`.
- Do not call `fetch` or `axios` directly inside components.
- Separate `DTO` and `UI model` through an adapter when needed.
- API contracts must have clear types in frontend `TypeScript`.

### 2. Response handling

- FE should only render data that has passed through an adapter.
- Do not couple components to the backend raw response.
- Always handle `loading`, `empty`, and `error`.

### 3. Auth and error handling

- Handle `401` consistently across the whole app.
- Do not show raw internal backend errors to users.
- Map `error.code` to display messages.

### 4. Query / Cache / Form

- After a mutation, invalidate or update the cache with the correct scope.
- FE validation only improves UX; it does not replace BE validation.

## API Review Checklist

- Does the endpoint use correct `resource naming`?
- Are the HTTP method and status code semantically correct?
- Are request/response objects separated from entities?
- Does the error format follow the shared standard?
- Does pagination follow the shared format?
- Does the new endpoint have OpenAPI and a confirmed FE/BE contract?

## References

- Google AIP-121: https://google.aip.dev/121
- Google AIP-122: https://google.aip.dev/122
- Zalando RESTful API Guidelines: https://opensource.zalando.com/restful-api-guidelines/
