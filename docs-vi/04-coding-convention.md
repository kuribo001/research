# Coding Convention

## Mục tiêu

Tài liệu này quy định coding convention bắt buộc cho Backend `Spring Boot` và Frontend `React` để codebase dễ đọc, dễ maintain, dễ review và đồng bộ với các tài liệu architecture, API và database.

## Nguyên tắc chung

- Tên biến, tên hàm, tên class phải rõ nghĩa hơn comment dài dòng.
- Mỗi hàm, mỗi class, mỗi component phải có trách nhiệm rõ ràng.
- Không được lặp lại business rule ở nhiều nơi.
- Mọi thay đổi mới phải tôn trọng architecture, API contract và database rule đã được chốt.
- Code mới phải kèm test hoặc phải có lý do rõ ràng nếu chưa thể test.
- Pull request phải có phạm vi vừa đủ, dễ review và dễ rollback nếu cần.

## Backend (Spring Boot 3.5 + Java 17)

### Quy tắc đặt tên bắt buộc

- Class dùng `PascalCase`.
- Method, field, biến local dùng `camelCase`.
- Constant dùng `UPPER_SNAKE_CASE`.
- Package dùng chữ thường, đặt theo module nghiệp vụ.
- Tên class phải phản ánh đúng vai trò, ví dụ `OrderController`, `OrderApplicationService`, `OrderRepositoryImpl`, `CustomerFacade`.
- Tên method phải bắt đầu bằng động từ và mô tả đúng hành vi.

### Cấu trúc code bắt buộc theo module

Backend bắt buộc tuân theo `modular architecture`, mỗi module gồm `api`, `application`, `domain`, `infrastructure`.

- Code HTTP đặt trong `api`.
- Use case, orchestration, transaction boundary đặt trong `application`.
- Business rule cốt lõi, value object, repository contract đặt trong `domain`.
- JPA repository implementation, external client, messaging, batch adapter đặt trong `infrastructure`.
- Không được tạo service, repository hoặc utility làm mờ ranh giới module.

### Quy tắc code Java bắt buộc

- Controller chỉ được xử lý HTTP concern như request mapping, validation, response status.
- Application service phải chứa orchestration, transaction, permission check và gọi dependency cần thiết.
- Domain không được phụ thuộc vào Spring framework, HTTP client, database implementation hoặc controller DTO.
- Repository chỉ được phụ trách truy cập dữ liệu.
- DTO, entity, domain model, response model không được dùng thay thế cho nhau.
- Phải sử dụng constructor injection. Không được dùng field injection.
- Không được tạo utility class tổng hợp business rule không có ranh giới nghiệp vụ.

### Quy tắc giao tiếp giữa các module

- Module A chỉ được gọi module B qua `application facade` hoặc interface đã được expose.
- Không được gọi trực tiếp vào `infrastructure` hoặc `domain internals` của module khác.
- Không được import request DTO, response DTO, entity nội bộ của module khác để tái sử dụng.
- Dữ liệu trao đổi giữa các module phải thông qua contract rõ ràng.
- Nghiệp vụ bất đồng bộ giữa các module phải đi qua event hoặc message đã được thiết kế rõ ràng.

### Spring convention bắt buộc

- `@Transactional` chỉ được đặt ở `application` layer hoặc facade dùng làm transaction boundary.
- Validation input phải đặt ở request DTO bằng `jakarta.validation`.
- Exception HTTP phải được mapping tập trung qua `@RestControllerAdvice`.
- Config class phải tách riêng theo concern như `security`, `web`, `datasource`, `messaging`, `batch`.
- Bean config phải đặt gần concern hoặc module liên quan, không được đặt tập trung một cách vô chủ đề.

### JPA, entity và database convention bắt buộc

- Entity được phép dùng để mapping với schema có sẵn của database.
- Entity phải map đúng bảng, cột, khóa chính, khóa ngoại và nullability theo schema thực tế.
- Không được sửa code để Hibernate tự tạo hoặc tự sửa schema.
- `spring.jpa.hibernate.ddl-auto` bắt buộc phải tắt.
- Không được sử dụng `create`, `create-drop`, `update` hoặc bất kỳ mode nào cho phép sinh DDL.
- Không được commit migration hoặc code có tác dụng thêm, sửa, xóa schema hiện có nếu chưa có phê duyệt chính thức.
- Repository implementation phải nằm ở `infrastructure`; repository contract phải nằm ở `domain`.

### API coding convention bắt buộc

- Controller phải dùng request DTO và response DTO. Không được expose entity trực tiếp ra API.
- JSON field phải dùng `camelCase`.
- Datetime trả về API phải theo `ISO-8601 UTC` với hậu tố `Z`.
- API error phải theo format thống nhất đã quy định trong `02-api-guideline.md`.
- Controller không được parse business rule hoặc thao tác trực tiếp với repository.

### Logging bắt buộc

- Log level phải dùng đúng mục đích: `ERROR`, `WARN`, `INFO`, `DEBUG`.
- Không được log password, token, OTP, secret, raw thông tin nhạy cảm.
- Log phải có context như `traceId`, `requestId`, `userId`, `resourceId` nếu có.
- Lỗi nghiệp vụ và lỗi hệ thống phải dễ debug được nhưng không lộ thông tin nội bộ cho client.

### Exception handling bắt buộc

- Không được ném `Exception`, `RuntimeException` chung chung cho business case.
- Business case quan trọng phải có custom exception rõ nghĩa.
- Mapping exception sang HTTP response phải thông qua handler tập trung.
- Message exception nội bộ không được dùng nguyên văn làm thông điệp hiển thị cho end user.

### Spring Batch convention bắt buộc

- `Spring Batch` phải tuân theo modular architecture.
- `reader`, `processor`, `writer`, `listener` phải đặt ở `infrastructure`.
- Business rule trong batch phải đặt ở `domain` hoặc `application`, không được nhét vào `reader`, `processor`, `writer` nếu đó là nghiệp vụ cốt lõi.
- Batch job không được gọi trực tiếp repository implementation của module khác.
- Batch job phải tái sử dụng `application facade` nếu chung luồng nghiệp vụ với API.

### Test bắt buộc cho Backend

- Business rule trong `domain` phải có unit test.
- `application` service phải có test cho use case chính, validation nghiệp vụ, transaction flow và error case quan trọng.
- Repository, security, API contract phải có integration test khi thay đổi liên quan.
- Batch job, batch step và idempotency phải có test nếu có thay đổi batch.
- Tên test phải mô tả hành vi, ví dụ `shouldReturn409WhenOrderAlreadyClosed`.

## Frontend (React + Vite + TypeScript + Tailwind CSS)

### Nguyên tắc chung bắt buộc

- Frontend bắt buộc sử dụng `React`, `Vite`, `TypeScript`.
- Code frontend bắt buộc tuân theo `feature-based architecture`.
- Không được thêm file JavaScript thường trong `src`, trừ file config của tool nếu cần.
- Component, hook, service, type phải đặt đúng ranh giới theo feature hoặc theo shared app-level.

### Quy tắc đặt tên bắt buộc

- Component file dùng `PascalCase`, ví dụ `UserTable.tsx`.
- Hook dùng prefix `use`, ví dụ `useUserList.ts`.
- Utility file dùng `kebab-case` và phải thống nhất trong toàn dự án.
- Type file dùng `kebab-case`, ví dụ `order-create-command.ts`, `user-summary.ts`.
- CSS module, test file, story file nếu có phải đặt tên sát với component.

### Cấu trúc code bắt buộc theo feature

- `pages` chỉ được compose page và route-level layout.
- `features` phải chứa code theo use case nghiệp vụ.
- `components` chỉ được chứa reusable UI component dùng chung.
- `shared` chỉ được chứa constant, util, token, base UI, type generic.
- `services` chỉ được chứa api client chung, interceptor, transport wrapper hoặc integration cấp ứng dụng.
- `hooks` chỉ được chứa custom hook dùng chung cấp app; hook nghiệp vụ phải đặt trong feature.
- `config` chỉ được chứa cấu hình frontend cấp ứng dụng.
- `routes` chỉ được chứa route declaration, route guard và route metadata cấp ứng dụng.
- `locales` chỉ được chứa resource i18n và locale config.
- `layouts` chỉ được chứa layout cấp page hoặc cấp app.
- Mỗi feature phải tự đóng gói UI, hook, service, type của chính nó.
- Feature phải có điểm expose rõ ràng qua `index.ts` nếu cần cho page hoặc feature khác sử dụng.

### Quy tắc component bắt buộc

- Page component chỉ được compose UI và gọi các feature cần thiết.
- Component không được chứa quá nhiều trách nhiệm.
- Props phải rõ ràng, tối giản và không gây mơ hồ.
- Không được truyền quá nhiều boolean flag để điều khiển nhiều mode trong một component lớn.
- Phải ưu tiên composition thay vì tăng nhanh số lượng `if`, `switch`, `variant` trong một component.
- Logic phức tạp phải tách khỏi JSX thành hook, function hoặc component con.

### State và effect bắt buộc

- Form có validation, submit flow hoặc mapping với API bắt buộc sử dụng `React Hook Form`.
- State nội bộ của component dùng local state nếu chỉ phục vụ riêng component đó.
- Không được đưa server state vào global UI store nếu state đó đã được quản lý đầy đủ ở API/query layer.
- `useEffect` chỉ được dùng cho side effect thực sự.
- Không được dùng `useEffect` để tính toán dữ liệu có thể tính trực tiếp trong render.
- Logic effect phức tạp phải tách thành custom hook.

### API và adapter bắt buộc

- Không được gọi trực tiếp `fetch` hoặc `axios` trong component.
- Mọi request phải đi qua `api client` hoặc service layer.
- Component không được parse raw API response.
- DTO từ backend phải được mapping sang UI model tại adapter hoặc service layer.
- Frontend phải xử lý đầy đủ `loading`, `empty`, `error` state.
- Mapping `error.code` từ backend sang thông điệp hiển thị phải nhất quán trong toàn bộ app.

### Style và UI bắt buộc

- Frontend phải sử dụng `Tailwind CSS` nhất quán trong toàn bộ codebase.
- Màu sắc, spacing, typography phải đi qua design token hoặc quy ước dùng chung.
- Không được hard-code style màu, khoảng cách, kích thước một cách tùy tiện nếu đã có token tương ứng.
- Không được hard-code text nếu màn hình nằm trong phạm vi có i18n requirement.

### Test bắt buộc cho Frontend

- Util, adapter, hook có business mapping phải có unit test.
- Component có interaction quan trọng phải có component test.
- Luồng nghiệp vụ chính phải có E2E test nếu thuộc phạm vi release quan trọng.
- Khi sửa bug, nếu có thể tái hiện bằng test, phải thêm test để tránh tái phát.

## Checklist review coding convention

- Tên biến, tên hàm, tên class, tên component đã rõ nghĩa chưa.
- Code có đúng ranh giới `api`, `application`, `domain`, `infrastructure` ở Backend không.
- Code có đúng ranh giới `pages`, `features`, `components`, `shared`, `services`, `hooks`, `config`, `routes`, `locales`, `layouts` ở Frontend không.
- Có vi phạm API contract, error format, datetime format hoặc naming convention không.
- Có vi phạm rule database như `ddl-auto`, migration schema hoặc entity map sai schema không.
- Business rule có bị lặp lại hoặc đặt sai layer không.
- Logging, exception handling và validation đã nhất quán chưa.
- Test đã cover đường nghiệp vụ chính và bug risk quan trọng chưa.
