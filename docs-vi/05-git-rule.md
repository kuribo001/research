# Git Rule

## Mục tiêu

Tài liệu này thống nhất cách làm việc với Git để team có quy trình phát triển, review và release ổn định.

## Branch

Branch chính và các môi trường tương ứng:

- `GDN-main`: môi trường `production`.
- `GDN-dev`: môi trường `develop`.

Branch phụ:

- `feature/<ticketID>-<short-name>`: phát triển tính năng mới, tách từ `GDN-dev`.
- `feature/<ticketID>-<short-name>-<child>`: tách từ nhánh `feature/*` gốc nếu nhiều người cùng làm 1 feature hoặc cần fix lỗi của feature đó.
- `bugfix/<ticketID>-<short-name>`: sửa lỗi trên luồng phát triển thông thường, cắt từ `GDN-dev`.
- `hotfix/<ticketID>-<short-name>`: sửa lỗi gấp cần đưa thẳng lên production, cắt từ `GDN-main`.

## Mapping branch và môi trường

- `GDN-dev` <-> `develop`
- `GDN-main` <-> `production`

## Nguyên tắc chung

- Không commit trực tiếp lên `GDN-main`, `GDN-dev`.
- Mọi thay đổi đi qua pull request.
- Pull request phải link ticket hoặc issue rõ ràng.
- Sử dụng `Squash merge`.

Quy ước `Conventional Commits`:

- Format chung: `<ticketID> <type>(<scope>): <short-description>`
- Nếu không cần `scope`, được phép dùng: `<ticketID> <type>: <short-description>`
- `ticketID` là bắt buộc và phải đặt ở đầu commit message.
- `short-description` phải ngắn gọn, rõ nghĩa, viết thường, mô tả đúng thay đổi.
- Có thể bổ sung nội dung chi tiết ở body nếu commit cần giải thích thêm lý do, phạm vi ảnh hưởng hoặc hướng migrate.

`type` được sử dụng:

- `feat`: thêm tính năng mới.
- `fix`: sửa lỗi.
- `refactor`: tái cấu trúc code, không đổi hành vi.
- `perf`: tối ưu hiệu năng.
- `test`: thêm hoặc sửa test.
- `docs`: cập nhật tài liệu.
- `build`: thay đổi liên quan build tool hoặc dependency build.
- `ci`: thay đổi pipeline CI/CD.
- `chore`: việc phụ trợ khác không thuộc các nhóm trên.
- `style`: sửa format code, khoảng trắng, lint, không đổi logic.

Ví dụ đúng:

- `GDN-123 feat(auth): add login API integration`
- `GDN-234 fix(order): handle duplicate order submission`
- `GDN-345 docs(git-rule): add conventional commits guideline`
- `GDN-456 test(customer): add unit test for customer facade`

## Quy trình feature

1. Tạo `feature/*` hoặc `bugfix/*` từ `GDN-dev`.
2. Code, test và cập nhật tài liệu nếu cần.
3. Tạo pull request vào `GDN-dev`.
4. Sau khi review và CI pass, merge.

Rule bổ sung cho feature branch:

- Branch `feature/*` hoặc `bugfix/*` phải được cập nhật code mới nhất từ `GDN-dev` mỗi ngày để tránh bị cũ nền code và giảm xung đột khi merge.
- Team phải chủ động `merge` hoặc `rebase` code mới nhất từ `GDN-dev` vào branch đang phát triển trước khi tiếp tục code nếu `GDN-dev` đã có thay đổi mới.
- Nếu 1 feature có nhiều người cùng tham gia phát triển, các branch con phải được tách từ branch `feature/*` của feature đó, không tách trực tiếp từ `GDN-dev`.
- Branch con chỉ được merge trở lại branch `feature/*` gốc của feature đó trước khi branch `feature/*` được merge vào `GDN-dev`.
- Branch `feature/*` gốc là nơi tổng hợp toàn bộ thay đổi của feature khi có nhiều người cùng làm.
- Nếu sau khi branch `feature/*` đã merge vào `GDN-dev` mà phát sinh lỗi, team phải fix trên chính branch `feature/*` đó và merge lại vào `GDN-dev`.
- Không được fix trực tiếp trên `GDN-dev` cho lỗi thuộc phạm vi một feature vẫn đang được phát triển trên branch `feature/*`.

## Quy trình lên Production

1. Khi tập hợp tính năng trên `GDN-dev` đã đạt điều kiện release, tạo pull request từ `GDN-dev` vào `GDN-main`.
2. Sau khi review và CI pass, merge vào `GDN-main`.
3. Deploy `GDN-main` lên môi trường `production`.
4. Gắn tag version nếu cần cho release production.

## Quy trình hotfix

1. Tạo `hotfix/*` từ `GDN-main`.
2. Fix, test nhanh, tạo pull request vào `GDN-main`.
3. Sau khi merge `GDN-main`, bắt buộc merge ngược lại `GDN-dev` để tránh lệch code.

## Quy trình đồng bộ branch

- Mọi thay đổi đã lên `GDN-main` phải được đồng bộ ngược lại `GDN-dev`.
- Không được sửa code trực tiếp trên môi trường runtime; mọi thay đổi vẫn phải đi qua branch trong Git.

## Checklist review code

- Ít nhất 1 reviewer bắt buộc.
- PR phải có `ticketID`, mô tả rõ mục tiêu thay đổi và phạm vi ảnh hưởng.
- PR lớn hơn 500 dòng code nên tách nhỏ nếu có thể.
- Reviewer ưu tiên tìm risk về hành vi, bảo mật, regression và missing test.
- Tác giả PR có trách nhiệm xử lý feedback và cập nhật mô tả nếu phạm vi đổi.

Reviewer phải kiểm tra các nhóm nội dung sau:

- Scope thay đổi có đúng với ticket và không trộn nhiều mục tiêu không liên quan.
- Tên branch, commit message, PR title và PR description có đúng `git rule` không.
- Code có đúng architecture và đúng layer đã được quy định trong `01-architecture.md` không.
- Business rule có đặt đúng chỗ, không bị lặp lại và không đổi hành vi ngoài phạm vi ticket không.
- Đặt tên biến, hàm, class, component, file có rõ nghĩa và đúng convention không.
- API contract có thay đổi không; nếu có thì có đúng `02-api-guideline.md` và có kế hoạch backward compatibility không.
- Backend có expose entity trực tiếp, đặt sai `transaction boundary`, gọi sai module hoặc đặt sai `application/domain/infrastructure` không.
- Frontend có đúng `feature-based architecture`, có gọi API trực tiếp trong component, parse raw response trong component hoặc đặt sai `pages/features/shared/services/hooks` không.
- Database có vi phạm `03-database-guideline.md`, bật sai `ddl-auto`, map sai entity, tạo thay đổi schema trái phép hoặc có nguy cơ N+1 / query kém hiệu năng không.
- Validation, permission check, security rule, log nhạy cảm và exception handling đã đúng chưa.
- Datetime, error format, status code, field naming và pagination có đúng convention không.
- Loading state, empty state, error state, optimistic update, responsive UI và browser chính đã được xử lý nếu thay đổi liên quan FE không.
- Logging, traceId, config mới, feature flag, health check, smoke test hoặc impact vận hành đã được xem xét không.
- Test đã đủ cho happy path, unhappy path, regression risk và bug fix không.
- Nếu sửa bug, đã có regression test hoặc lý do rõ ràng nếu chưa thể thêm test không.

Reviewer cần đặt câu hỏi nếu gặp một trong các dấu hiệu sau:

- Code dùng những workaround khó giải thích hoặc comment thay cho thiết kế rõ ràng.
- Một class, component, hook hoặc service làm quá nhiều việc.
- PR đổi nhiều file nhưng mô tả không giải thích đủ tác động.
- Có thay đổi contract, config, query, security hoặc deployment behavior nhưng không được nhắc trong PR.
