# Database Guideline

## Mục tiêu

Tài liệu này xác lập nguyên tắc thiết kế dữ liệu, migration và cách frontend tương tác với dữ liệu một cách ổn định.

## Nguyên tắc chung

- Ưu tiên relational database cho nghiệp vụ giao dịch.
- Hệ thống sử dụng database có sẵn, team không được thêm, sửa, xóa schema hiện có.
- Không sửa tay schema trên môi trường database.
- Dữ liệu là tài sản lâu dài, mọi thay đổi cần ưu tiên backward compatibility.

## Backend (Spring Boot)

### Thiết kế schema

- Team không được thêm bảng mới, sửa cấu trúc bảng, đổi kiểu dữ liệu, thêm cột, sửa cột, xóa cột, thêm constraint, sửa constraint, xóa constraint nếu chưa có phê duyệt riêng từ owner của database.
- Team không được tự ý thay đổi schema thông qua migration tool hoặc Hibernate auto DDL.
- Tên bảng dùng `snake_case`, dạng số nhiều nếu là bảng nghiệp vụ, ví dụ `users`, `orders`.
- Tên cột dùng `snake_case`.
- Khóa chính thống nhất một chuẩn, ưu tiên `bigint` hoặc `uuid` tùy nhu cầu.
- Có các cột cơ bản khi phù hợp: `created_at`, `created_by`, `updated_at`, `updated_by`.
- Dùng foreign key khi cần đảm bảo toàn vẹn dữ liệu.

### Chuẩn hóa và mô hình dữ liệu

- Ưu tiên tối thiểu 3NF cho hệ thống giao dịch.
- Chỉ denormalize khi có lý do rõ ràng về performance hoặc reporting.
- Trường JSON trong relational DB chỉ dùng cho metadata linh hoạt, không thay thế schema nghiệp vụ cốt lõi.

### Migration

- Trong phạm vi dự án này, team không được dùng `Flyway` hoặc `Liquibase` để tạo, sửa, xóa schema hiện có.
- Không được commit migration làm thay đổi schema nếu chưa có yêu cầu và phê duyệt chính thức.
- Nếu database owner cung cấp thay đổi schema, team phải cập nhật entity, query và test theo schema mới thay vì tự sinh schema.

### Entity và ORM

- Backend được phép và được khuyến khích sử dụng entity để mapping với bảng có sẵn trong database.
- Entity phải map đúng tên bảng, tên cột, khóa chính, khóa ngoại và ràng buộc nullability theo schema thực tế.
- Không được giả định Hibernate sẽ tự tạo hoặc tự sửa bảng để làm entity chạy được.
- `spring.jpa.hibernate.ddl-auto` bắt buộc phải tắt. Giá trị được phép là `none` hoặc cấu hình tương đương không sinh DDL.
- Không được sử dụng `create`, `create-drop`, `update` hoặc bất kỳ cấu hình nào cho phép Hibernate thay đổi schema.
- Mọi entity mới hoặc entity sửa lại phải được đối chiếu với schema thực tế trước khi merge code.
- Nếu bảng có sẵn không phù hợp với convention thông thường, entity phải mapping theo schema thực tế thay vì sửa schema cho đẹp code.

### Index

- Tạo index dựa trên query thực tế, không index theo cảm tính.
- Mọi foreign key quan trọng nên được xem xét index.
- Kiểm tra trade-off giữa tốc độ đọc và chi phí ghi.

### Transaction

- Transaction boundary đặt ở service/application layer.
- Giữ transaction ngắn và rõ ràng.
- Không thực hiện network call lâu bên trong transaction nếu có thể tránh.

### Soft delete và audit

- Soft delete chỉ dùng khi nghiệp vụ cần truy vết hoặc khôi phục.
- Nếu dùng soft delete, cần thống nhất tên cột, ví dụ `deleted_at`.
- Audit log cho các thao tác nhạy cảm nên tách khỏi bảng nghiệp vụ chính.

### Hiệu năng và bảo trì

- Không dùng `SELECT *`.
- Tránh N+1 query; theo dõi bằng log SQL, profiler hoặc APM.
- Tách read model cho báo cáo nếu query nghiệp vụ quá phức tạp.

## Frontend (React)

### Nguyên tắc quản lý dữ liệu phía client

- Frontend không sở hữu schema database.
- FE chỉ làm việc với API contract và model đã adapter.
- Không đưa logic phân tích trực tiếp từ schema backend vào component.

### Local storage, session storage, cache

- Chỉ lưu dữ liệu tối thiểu cần thiết trên client.
- Không lưu secret, password, access token lâu dài ở local storage nếu có lựa chọn an toàn hơn.
- Định nghĩa rõ dữ liệu nào là persistent cache, dữ liệu nào là tạm thời.

### Định dạng dữ liệu

- Chuẩn hóa model client khi cần render lại ở nhiều nơi.
- Date/time phải convert thống nhất tại adapter layer.
- Enum từ backend cần có mapping rõ ràng sang label UI.

### Đồng bộ với backend

- Khi backend thêm field mới, FE phải treat field đó là optional cho đến khi rollout xong.
- Khi backend đổi nghĩa field, FE không deploy dựa trên giả định mà chưa có versioning hoặc feature flag.

## Checklist review database

- Schema có dễ hiểu và phản ánh đúng nghiệp vụ không.
- Code có vô tình tạo thay đổi schema qua migration hoặc `ddl-auto` không.
- Entity có map đúng schema thực tế không.
- Query chính đã được xem xét index không.
- Có nguy cơ N+1, lock lâu hoặc dirty write không.
- FE có đang phụ thuộc quá chặt vào chi tiết lưu trữ của BE không.
