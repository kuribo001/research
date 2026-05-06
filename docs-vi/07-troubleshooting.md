# Troubleshooting Guide

## Mục tiêu

Tài liệu này tổng hợp cách xử lý sự cố phổ biến trong quá trình phát triển, deploy và vận hành.

## Nguyên tắc chung

- Tái hiện được lỗi trước khi sửa.
- Ưu tiên thu thập bằng chứng: log, metric, trace, screenshot, request payload.
- Tách rõ lỗi thuộc backend, frontend hay contract giữa hai bên.
- Ghi lại root cause và hướng xử lý để tránh lặp lại.

## Quy trình xử lý sự cố

1. Xác định phạm vi ảnh hưởng.
2. Kiểm tra thay đổi gần nhất: code, config, schema do khách hàng cung cấp, data.
3. Thu thập log và trace theo `traceId` nếu có.
4. Tái hiện trên `local`, `develop` hoặc môi trường đang gặp sự cố.
5. Khoanh vùng theo lớp: FE, API, DB, external service.
6. Sửa lỗi, bổ sung test, cập nhật tài liệu nếu cần.

## Backend (Spring Boot)

### Ứng dụng không startup được

- Kiểm tra log lúc boot.
- Xác minh env var, profile đang chạy, port, datasource.
- Kiểm tra thay đổi schema do khách hàng cung cấp hoặc cấu hình datasource có bị lệch không.
- Kiểm tra bean config, circular dependency, classpath.

### Lỗi kết nối database

- Kiểm tra URL, username, password, network access.
- Xác minh schema khách hàng cung cấp đã tồn tại, đúng version và phù hợp với entity mapping hiện tại.
- Kiểm tra connection pool có bị exhausted không.

### API response chậm

- Kiểm tra log SQL, N+1 query, external API latency.
- Xem metric CPU, memory, thread pool, DB slow query.
- Kiểm tra transaction có bị giữ quá lâu không.

### Lỗi `4xx` hoặc `5xx`

- `400/422`: kiểm tra payload, validation, business rule.
- `401/403`: kiểm tra auth token, role, permission mapping.
- `500`: đối chiếu exception stacktrace với request, traceId, external dependency.

## Frontend (React)

### Ứng dụng không build được

- Kiểm tra dependency, env var, import path, type error.
- Kiểm tra sự khác nhau giữa local và CI node version.
- Nếu lỗi sau khi đổi contract API, kiểm tra adapter và type.

### UI không hiện dữ liệu

- Kiểm tra request trong browser network tab.
- Xác minh query key, cache state, loading state, error state.
- Kiểm tra adapter có mapping đúng field từ API không.

### Lỗi CORS hoặc auth

- Xác minh origin của frontend đã được BE cho phép.
- Kiểm tra token có được gửi đúng header không.
- Kiểm tra flow refresh token hoặc redirect login.

### Hành vi UI không ổn định

- Kiểm tra race condition giữa nhiều request.
- Kiểm tra dependency array của `useEffect`.
- Xác minh component có đang giữ state cũ sau khi route thay đổi không.

## Vấn đề giao tiếp giữa BE và FE

### Sai contract

- So sánh payload thực tế với OpenAPI hoặc mock contract.
- Kiểm tra field bắt buộc, enum, date format, pagination.
- Nếu BE đổi contract, cần xác định có versioning hay feature flag không.

### Lỗi chỉ xảy ra trên production

- Kiểm tra config khác biệt giữa environment.
- Kiểm tra build FE có đúng API base URL không.
- Kiểm tra secret, CORS, CDN cache, thay đổi schema từ phía khách hàng đã được áp dụng đầy đủ chưa.

## Hậu kiểm

- Bổ sung test cho lỗi vừa gặp.
- Cập nhật tài liệu hoặc alert nếu cần.
- Tạo postmortem cho sự cố nghiêm trọng.
