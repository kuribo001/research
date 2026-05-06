# Testing Guideline

## Mục tiêu

Tài liệu này quy định chiến lược test bắt buộc cho Backend `Spring Boot` và Frontend `React` để bảo đảm code đúng nghiệp vụ, dễ refactor và giảm regression.

## Nguyên tắc chung

- Test là một phần bắt buộc của thay đổi code, không phải việc làm sau.
- Mỗi thay đổi nghiệp vụ, thay đổi contract hoặc sửa bug phải được đánh giá nhu cầu bổ sung test.
- Test phải phản ánh hành vi cần bảo vệ, không chỉ test để tăng coverage.
- Test phải ổn định, có thể chạy lặp lại và không phụ thuộc vào thứ tự chạy ngẫu nhiên.
- Test fail phải chỉ ra rõ nguyên nhân để dễ debug.

## Định nghĩa các loại test

### Unit test

- Unit test kiểm tra một đơn vị nhỏ của code như hàm, class, hook, adapter, domain service.
- Unit test phải chạy nhanh, độc lập và không phụ thuộc database thật, network thật hoặc framework context lớn.
- Unit test được dùng để bảo vệ business rule, utility logic, mapping logic và edge case nhỏ.

### Integration test

- Integration test kiểm tra sự kết hợp giữa nhiều thành phần thật với nhau.
- Integration test được dùng cho repository, API contract, security, serialization, database query, transaction flow, message integration và batch flow.
- Integration test được phép dùng Spring context, database test hoặc Testcontainers nếu cần.

### Component test

- Component test kiểm tra hành vi của React component trong ngữ cảnh render thực tế.
- Component test tập trung vào interaction, state thay đổi, loading, empty, error state và rendering theo dữ liệu đầu vào.

### End-to-End test

- E2E test kiểm tra user flow chính từ đầu đến cuối trên ứng dụng gần giống môi trường thật.
- E2E test được dùng để bảo vệ các luồng nghiệp vụ quan trọng, luồng release risk cao và integration xuyên FE/BE.

## Backend (Spring Boot 3.5 + Java 17 + Spring Batch)

### Rule test bắt buộc theo layer

- `domain` phải được bảo vệ bằng unit test.
- `application` phải có test cho use case chính, validation nghiệp vụ, transaction flow và error case quan trọng.
- `infrastructure` phải có integration test khi thay đổi repository, query, external integration, message flow hoặc persistence behavior.
- `api` phải có controller hoặc API integration test khi thay đổi endpoint, request DTO, response DTO, validation, error format, auth hoặc serialization.
- `batch` phải có test riêng cho job, step, idempotency, retry, skip và mapping logic nếu có thay đổi liên quan.

### Khi nào bắt buộc thêm unit test cho Backend

- Thêm hoặc sửa business rule trong `domain`.
- Thêm hoặc sửa `application service` có decision logic.
- Thêm hoặc sửa utility hoặc mapper có xử lý nghiệp vụ.
- Sửa bug có thể tái hiện ở mức hàm, class hoặc service nhỏ.

### Khi nào bắt buộc thêm integration test cho Backend

- Thêm hoặc sửa repository, custom query, native query hoặc mapping JPA.
- Thêm hoặc sửa endpoint API.
- Thêm hoặc sửa security rule, auth flow, permission check.
- Thêm hoặc sửa transaction boundary, persistence behavior, lazy/eager loading, lock strategy.
- Thêm hoặc sửa batch job, batch step, reader, writer, processor.
- Thêm hoặc sửa external client mà ứng dụng phụ thuộc trực tiếp.

### Rule cho database test

- Không được bật `ddl-auto` để test theo kiểu tự sinh schema trái rule dự án.
- Integration test liên quan database phải bám schema thực tế hoặc test setup tương đương schema thực tế.
- Test query quan trọng phải kiểm tra đúng kết quả, đúng filter, đúng sort và hạn chế N+1 nếu có nguy cơ.
- Khi sửa entity mapping, test phải bảo vệ tên cột, nullability và quan hệ chính.

### Rule cho API test

- API test phải kiểm tra status code, response body, error format, validation và datetime format khi có thay đổi liên quan.
- API test không được chỉ kiểm tra `200 OK`; phải có case lỗi nếu endpoint có nghiệp vụ validate hoặc phân quyền.
- Nếu thay đổi API contract, test phải bảo vệ contract mới và không được bỏ qua backward compatibility risk.

### Rule cho batch test

- Batch test phải bảo vệ logic xử lý từng bản ghi, tổng kết quả job, retry/skip behavior và idempotency.
- Nếu batch có đọc/ghi database, phải có test cho dữ liệu biên và dữ liệu lỗi.
- Batch test không được chỉ test job chạy thành công; phải có case lỗi nghiệp vụ quan trọng nếu có.

## Frontend (React + Vite + TypeScript + Tailwind CSS)

### Rule test bắt buộc theo loại code

- `util`, `adapter`, `formatter`, `mapper`, `custom hook` có business logic phải có unit test.
- React component có interaction quan trọng phải có component test.
- Feature có loading, empty, error, submit flow hoặc state transition quan trọng phải có test bảo vệ.
- Luồng nghiệp vụ chính hoặc release risk cao phải có E2E test.

### Khi nào bắt buộc thêm unit test cho Frontend

- Thêm hoặc sửa hook có logic nghiệp vụ.
- Thêm hoặc sửa adapter mapping DTO sang UI model.
- Thêm hoặc sửa util xử lý date, enum, money, validation, filter, transform.
- Sửa bug có thể tái hiện ở mức function, hook hoặc adapter.

### Khi nào bắt buộc thêm component test cho Frontend

- Thêm hoặc sửa component form, modal, table, filter, pagination, stateful widget.
- Thêm hoặc sửa component có loading state, empty state, error state.
- Thêm hoặc sửa interaction như click, submit, change input, retry, toggle, select.
- Thêm hoặc sửa component mà business flow phụ thuộc vào trạng thái render.

### Khi nào bắt buộc thêm E2E test

- Thêm hoặc sửa user flow chính như đăng nhập, tạo mới, cập nhật, xóa, gửi dữ liệu, thanh toán, đồng bộ batch trigger nếu có UI.
- Sửa bug từng xuất hiện trên môi trường gần production mà component test không đủ bảo vệ.
- Release có risk cao do liên quan đồng thời FE, BE và contract.

### Rule cho API mocking ở Frontend

- Unit test và component test được phép mock API để bảo vệ hành vi component.
- Không được để component test phụ thuộc hoàn toàn vào backend thật nếu mục tiêu chỉ là kiểm tra UI behavior.
- Mock response phải bám API contract thực tế.
- Nếu API contract thay đổi, mock test liên quan phải được cập nhật cùng lúc.

## Rule khi sửa bug

- Mỗi bug fix phải được đánh giá khả năng tái hiện bằng test.
- Nếu bug có thể tái hiện bằng test, team phải thêm regression test trước hoặc cùng lúc với fix.
- Nếu chưa thể thêm test, PR phải nêu rõ lý do và rủi ro còn lại.

## Rule về dữ liệu test

- Test data phải rõ nghĩa, dễ đọc và phản ánh đúng nghiệp vụ.
- Không hard-code test data khó hiểu hoặc phụ thuộc mutating state giữa nhiều test.
- Mỗi test phải có setup/teardown rõ ràng nếu có sử dụng resource dùng chung.
- Không được để test phụ thuộc vào dữ liệu tồn tại sẵn trong môi trường ngoài.

## Rule đặt tên test

- Tên test phải mô tả hành vi cần bảo vệ.
- Backend test nên theo dạng `should...When...`.
- Frontend test nên theo hành vi người dùng hoặc kết quả render mong đợi.

Ví dụ:

- `shouldReturn409WhenOrderAlreadyClosed`
- `shouldSaveOrderWhenInputIsValid`
- `shouldShowErrorMessageWhenLoginFails`
- `shouldDisableSubmitButtonWhileSubmitting`

## Rule chạy test trong pipeline

- Pull request phải chạy bộ test tối thiểu phù hợp với phạm vi thay đổi.
- Merge lên branch chính phải có kết quả CI pass.
- Build deploy lên môi trường phải đi kèm test và smoke check theo `06-deployment.md`.
- Test fail trong CI không được bỏ qua nếu chưa có phê duyệt và lý do rõ ràng.

## Checklist review test

- Thay đổi này có cần thêm unit test không.
- Thay đổi này có cần thêm integration test không.
- Sửa bug này đã có regression test chưa.
- Test có bảo vệ happy path và unhappy path không.
- Test có dễ đọc, ổn định và không flaky không.
- Mock có bám contract thực tế không.
- Integration test có thực sự kiểm tra được persistence, API, security hoặc flow cần bảo vệ không.
- Có bỏ sót loading, empty, error state hoặc permission case không.
- Test mới có phản ánh đúng mục tiêu ticket và risk của thay đổi không.
