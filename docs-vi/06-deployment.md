# Deployment Guideline

## Mục tiêu

Tài liệu này quy định cách build, release, deploy và rollback cho backend Spring Boot và frontend React.

## Nguyên tắc chung

- Mỗi environment phải có config tách biệt.
- Build artifact phải có thể trace về commit/tag.
- Quy trình deploy phải có health check và rollback plan.
- Không được sửa tay production nếu có thể giải quyết bằng pipeline.

## Môi trường bắt buộc

- `local`: môi trường phát triển cá nhân.
- `develop`: môi trường tích hợp nội bộ, map với branch `GDN-dev`.
- `production`: môi trường phục vụ người dùng thật, map với branch `GDN-main`.

## Backend (Spring Boot)

### Build artifact

- Backend phải được đóng gói thành `jar` hoặc container image.
- Version artifact gắn với git commit hash hoặc release tag.
- Cấu hình runtime thông qua environment variable, không hard-code.

### Trình tự deploy

1. Chạy test và static analysis trên CI.
2. Build artifact.
3. Kiểm tra cấu hình database và xác nhận không có thay đổi schema trái rule dự án.
4. Deploy lên environment đích.
5. Kiểm tra `health`, `readiness`, smoke test API.

### Cấu hình

- Tách `application-local.yml`, `application-develop.yml`, `application-prod.yml`.
- Secret đưa vào secret manager hoặc biến môi trường.
- Production bắt buộc phải có logging và metric.

### Rollback

- Phải ưu tiên `roll-forward` nếu migration đã chạy và không dễ rollback an toàn.
- Nếu rollback được, phải kiểm tra tính tương thích schema và artifact.
- Cần ghi rõ migration nào là one-way migration.

## Frontend (React)

### Build artifact

- Frontend phải được build thành static asset, ví dụ `dist/` hoặc `build/`.
- Asset filename phải có hash để phục vụ cache busting.
- Source map cho production phải được quản lý an toàn.

### Cấu hình

- Biến môi trường chỉ chứa giá trị an toàn cho client.
- Không đưa secret backend vào frontend build.
- Base URL API và feature flag được quản lý theo từng environment.

### Deploy

1. Chạy lint, unit test, build.
2. Publish static asset lên hosting/CDN.
3. Invalidate cache nếu cần.
4. Chạy smoke test cho user flow quan trọng.

### Rollback

- Rollback bằng cách phục hồi bản build trước đó.
- Nếu frontend phụ thuộc contract API mới, cần đảm bảo backend vẫn tương thích với bản FE cũ trong thời gian rollback.

## Phụ thuộc giữa BE và FE

- Backend không được xóa contract cũ trước khi frontend rollout xong.
- Frontend phải cho phép backend thêm field mới mà không gây lỗi render.
- Các release có ảnh hưởng hai chiều phải được đặt sau feature flag nếu nghiệp vụ cho phép.

## Checklist deployment

- Đã cập nhật config đúng environment chưa.
- Branch deploy có đúng mapping với environment không:
  `GDN-dev` -> `develop`, `GDN-main` -> `production`.
- Đã có health check và smoke test chưa.
- Migration database có an toàn không.
- FE và BE có tương thích contract không.
- Đã có kế hoạch rollback/roll-forward chưa.
