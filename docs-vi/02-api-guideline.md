# API Guideline

## Mục tiêu

Tài liệu này là chuẩn API bắt buộc cho cả team Backend `Spring Boot` và Frontend `React`.

## Nguyên tắc chung

- API là contract chung giữa BE và FE.
- Ưu tiên đơn giản, nhất quán, dễ đoán.
- Mọi thay đổi có nguy cơ break contract phải được review và thông báo rõ.
- Request, response, error format và pagination phải thống nhất toàn hệ thống.

## Backend (Spring Boot)

### 1. Endpoint naming

- Bắt buộc dùng `RESTful resource naming`.
- URL dùng `danh từ`, không dùng `động từ`.
- Collection dùng danh từ số nhiều: `/users`, `/orders`, `/products`.
- Resource detail dùng `/{resource}/{id}`: `/users/{userId}`.
- Sub-resource chỉ dùng khi có quan hệ cha-con rõ ràng: `/users/{userId}/addresses`.
- Không đặt tên như: `/getUsers`, `/createOrder`, `/updateProfile`, `/deleteProduct`.
- Query param chỉ dùng cho `filter`, `sort`, `search`, `pagination`.
- Chỉ dùng action endpoint khi nghiệp vụ không map được vào CRUD: `/orders/{orderId}/cancel`.

Đúng:

- `/api/v1/users`
- `/api/v1/users/{userId}`
- `/api/v1/orders/{orderId}/items`
- `/api/v1/orders/{orderId}/cancel`

Sai:

- `/api/v1/get-user-list`
- `/api/v1/user/detail/{userId}`
- `/api/v1/getAddressesByUserId/{userId}`
- `/api/v1/cancelOrder/{orderId}`

### 2. HTTP method

- `GET`: lấy dữ liệu.
- `POST`: tạo mới hoặc thực thi action không idempotent.
- `PUT`: thay thế toàn bộ resource.
- `PATCH`: cập nhật một phần resource bằng partial DTO, không dùng JSON Patch operation array.
- `DELETE`: xóa resource.

### 3. Request / Response

- Bắt buộc dùng DTO, không expose entity trực tiếp.
- JSON field dùng `camelCase`.
- Date/time dùng ISO-8601, bắt buộc UTC.
- Response chỉ trả dữ liệu cần thiết cho client.
- Không trả field nội bộ, secret hoặc thông tin nhạy cảm.

Format `datetime` JSON bắt buộc:

- Dùng chuẩn `ISO-8601`.
- Bắt buộc trả về UTC với hậu tố `Z`.
- Field có thời điểm đầy đủ dùng format: `yyyy-MM-dd'T'HH:mm:ss'Z'`.
- Không dùng format mơ hồ như `22/06/2026 17:30`, `2026/06/22`, `06-22-2026`, chuỗi không có timezone hoặc datetime không phải UTC.

Đúng:

- `2026-06-22T10:30:00Z`

Sai:

- `2026-06-22 10:30:00`
- `22/06/2026 10:30`
- `2026-06-22T10:30:00`
- `2026-06-22T17:30:00+07:00`

Mẫu `JSON response` đúng:

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

Lưu ý:

- List response có thể dùng `summary DTO`, không bắt buộc giống 100% detail response.
- Detail response và list response phải được đặt tên field nhất quán nếu cùng biểu diễn cùng một thuộc tính.

### 4. Status code

- `200`: thành công có body.
- `201`: tạo mới thành công.
- `204`: thành công không có body.
- `400`: sai format request hoặc validation fail.
- `401`: chưa xác thực.
- `403`: không đủ quyền.
- `404`: không tìm thấy resource.
- `409`: xung đột state/dữ liệu.
- `422`: request đúng format nhưng sai nghiệp vụ.
- `500`: lỗi hệ thống.

### 5. Error format

Tất cả API lỗi phải theo 1 format chung:

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

Rule bắt buộc:

- `code`: dùng để FE mapping thông điệp.
- `message`: ngắn gọn, không lộ thông tin nội bộ.
- `details`: là optional, chỉ dùng khi cần trả lỗi validation hoặc field-level error.
- `traceId`: bắt buộc để debug.

### 6. Pagination / Filter / Sort

- Query param chuẩn: `page`, `size`, `sort`.
- `page` bắt buộc là `0-based`.
- `size` là số bản ghi trên mỗi trang.
- `sort` dùng format `field,direction`, ví dụ `createdAt,desc`.
- Nếu có nhiều điều kiện sort, lặp lại query param `sort`, ví dụ `?sort=status,asc&sort=createdAt,desc`.
- Filter đặt tên theo nghiệp vụ: `status`, `fromDate`, `toDate`.
- Format response pagination phải thống nhất:

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

- Validation input đặt ở request DTO bằng `jakarta.validation`.
- Business validation xử lý ở service/application layer.
- Auth header theo chuẩn `Authorization: Bearer <token>`.
- Không log token, password, OTP, secret.
- Bắt buộc có OpenAPI/Swagger cho endpoint mới hoặc endpoint thay đổi.

## Frontend (React)

### 1. Cách gọi API

- Mọi request đi qua một `api client/service` chung.
- Không gọi trực tiếp `fetch` hoặc `axios` trong component.
- Tách `DTO` và `UI model` qua adapter nếu cần.
- Contract API phải có type rõ ràng trong frontend `TypeScript`.

### 2. Cách xử lý response

- FE chỉ render dữ liệu đã qua adapter.
- Không coupling component với raw response của backend.
- Phải xử lý đầy đủ `loading`, `empty`, `error`.

### 3. Auth và error handling

- Xử lý `401` nhất quán trong toàn bộ app.
- Không hiện raw error nội bộ từ backend cho người dùng.
- Mapping `error.code` sang thông điệp hiển thị.

### 4. Query / Cache / Form

- Sau mutation phải invalidate hoặc update cache đúng phạm vi.
- Validation FE chỉ hỗ trợ UX, không thay thế validation BE.

## Checklist review API

- Endpoint có đúng `resource naming` không.
- HTTP method và status code có đúng nghĩa không.
- Request/response đã tách khỏi entity chưa.
- Error format có đúng chuẩn chung không.
- Pagination có đúng format thống nhất không.
- Endpoint mới đã có OpenAPI và được FE/BE thống nhất contract chưa.

## References

- Google AIP-121: https://google.aip.dev/121
- Google AIP-122: https://google.aip.dev/122
- Zalando RESTful API Guidelines: https://opensource.zalando.com/restful-api-guidelines/
