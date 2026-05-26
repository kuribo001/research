# Japan Rail Demo Plan

Cập nhật: 2026-05-26

## 1. Mục Tiêu Demo

Demo tập trung vào 3 mục tiêu:

1. Lấy danh sách trạm/ga của `Tokyo`, `Miyazaki`, `Shimane`.
2. Vẽ các tuyến đường sắt và trạm/ga lên `Cesium`.
3. Lấy `trip` và thông tin `realtime` của tàu theo mức dữ liệu nguồn cho phép.

Stack dự kiến:

- FE: ReactJS
- BE: Spring Boot
- AWS Lambda crawler: Java

## 2. Quyết Định Nguồn Dữ Liệu

- `Station catalog`: dùng `MLIT N02 + N03`.
- `Line để vẽ Cesium`: dùng `MLIT N02 RailroadSection` cho demo hiện tại.
- `Tokyo trip/realtime`: chốt `Toei`, dùng ODPT.
- `Miyazaki trip static`: dùng `JR Kyushu timetable portal/PDF`.
- `Shimane trip/realtime`: tập trung `JR West`, dùng JR West timetable / train-guide.

## 3. Scope Theo Phase

### Phase 1

- Lấy station list cho `Tokyo`, `Miyazaki`, `Shimane`.
- Vẽ railway lines và stations lên `Cesium`.
- Dùng dữ liệu MLIT để chạy demo nhanh.

### Phase 2

- Làm `trip/realtime` cho `Tokyo / Toei`.
- Parse `ODPT GTFS/GTFS-JP`, `Train Location`, `GTFS-RT`.
- Normalize thành model chung cho backend.

### Phase 3

- Mở rộng `Miyazaki / JR Kyushu` bằng timetable parser nếu cần.
- Mở rộng `Shimane / JR West` bằng timetable / train-guide parser nếu cần.

## 4. Kiến Trúc Đề Xuất

`ODPT / MLIT / operator sources -> Lambda crawler -> S3 raw zone -> Parser/Normalizer -> PostgreSQL(PostGIS) -> Spring Boot API -> React Cesium UI`

Lý do cần `raw + normalized`:

- Raw zone giúp debug khi feed đổi schema.
- Normalized zone giúp FE/BE không phụ thuộc trực tiếp vào từng nguồn.
- PostGIS phù hợp để lưu station, line geometry, và query map.

## 5. Model Dữ Liệu Chính

- `prefectures`
- `operators`
- `operator_source_mappings`
- `stations`
- `rail_lines`
- `rail_line_sections`
- `trips`
- `trip_stops`
- `train_positions`
- `train_status_events`

Nguyên tắc operator:

- Không dùng tên operator từ MLIT như cross-source id chính thức.
- Dùng `internal_operator_key` và `source_mappings`.
- Ví dụ: `mlit:toei`, `mlit:jr_kyushu`, `mlit:jr_west`.

## 6. API Gợi Ý

- `GET /api/prefectures`
- `GET /api/prefectures/{id}/stations`
- `GET /api/prefectures/{id}/rail-lines`
- `GET /api/operators/{id}/trips`
- `GET /api/operators/{id}/trains/live`
- `GET /api/trains/{id}`

## 7. Timeline Demo

### Giai Đoạn 0

- Chốt scope.
- Đăng ký ODPT account/API key.
- Chuẩn bị DB schema và mock API.

### Giai Đoạn 1

- Ingest `MLIT N02/N03`.
- Export station list và railway line geometry.
- Verify Cesium viewer.

### Giai Đoạn 2

- Ingest `Toei GTFS/GTFS-JP`.
- Parse `routes.txt`, `trips.txt`, `stop_times.txt`, `stops.txt`, `shapes.txt`.
- Xuất JSON normalized cho trip.

### Giai Đoạn 3

- Ingest `Toei Train Location / GTFS-RT`.
- Tạo API live train.
- Hiển thị `real`, `estimated`, hoặc `status only`.

### Giai Đoạn 4

- Hoàn thiện UI demo.
- Thêm filter theo prefecture/operator/line.
- Thêm popup station/train.

## 8. Rủi Ro

- ODPT API key có thể mất thời gian duyệt.
- Không phải operator nào cũng có GPS thật.
- `JR Kyushu` và `JR West` chưa có public GTFS/ODPT trip dataset đã xác minh.
- Realtime web/app service chưa đồng nghĩa có public machine-readable API.
- License và terms cần kiểm tra trước khi crawl tự động.

## 9. Definition Of Done

- Có station list cho `Tokyo`, `Miyazaki`, `Shimane`.
- Có railway lines render được trên Cesium.
- Có Toei trip data normalized.
- Có Toei live train/status data ở mức nguồn cho phép.
- Docs ghi rõ nguồn nào dùng được, nguồn nào chưa xác minh.

## 10. Nguồn Chính

- MLIT N02: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
- MLIT N03: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
- ODPT developer: https://developer.odpt.org/
- Toei GTFS: https://ckan.odpt.org/ja/dataset/train-toei
- Toei train location: https://ckan.odpt.org/dataset/r_train_location-toei
- Toei GTFS-RT: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei
- JR Kyushu timetable: https://www.jrkyushu-timetable.jp/
- JR West timetable: https://www.westjr.co.jp/global/en/timetable/
- JR West train-guide: https://www.train-guide.westjr.co.jp/area_sanin.html
