# Research Presentation

Cập nhật: 2026-05-26

## 1. Bài Toán

Demo railway cho `Tokyo`, `Miyazaki`, `Shimane` cần 3 đầu ra:

1. Station catalog.
2. Railway lines trên `Cesium`.
3. `Trip` và `realtime train position`.

## 2. Kết Luận Tổng Quan

Không có một nguồn duy nhất phù hợp cho tất cả.

Hướng dùng:

- `MLIT` cho station catalog và line nền.
- `ODPT` cho trip/realtime ở `Tokyo`.
- Nguồn local/operator cho `Miyazaki` và `Shimane` khi GTFS công khai không sẵn.

## 3. Quyết Định Nguồn

- `Station`: `MLIT N02 + N03`.
- `Line`: `MLIT N02 RailroadSection`.
- `Tokyo trip/realtime`: `Toei` qua `ODPT`.
- `Miyazaki trip/realtime`: `JR Kyushu timetable portal/PDF` + `Train Navi`.
- `Shimane trip/realtime`: `JR West` timetable/pages + running-position web service.

## 4. Theo Prefecture

### Tokyo

- Khả năng thành công cao nhất cho `trip/realtime`.
- Đã chốt `Toei` làm operator chính.

### Miyazaki

- `GTFS Data Repository` đã kiểm tra và trả rỗng cho `pref=45`.
- `trip static` nên lấy từ `JR Kyushu timetable portal/PDF`.
- Khi normalize `trip`, bổ sung station directory, route map, station map browser.
- `realtime` chưa xác minh được machine-readable feed công khai.

### Shimane

- Tập trung vào `JR West`.
- ODPT search ngày `2026-05-26` không tìm thấy dataset `jrwest` / `西日本旅客鉄道`.
- `trip static` phải đi theo JR West timetable / route pages, không phải ODPT.
- Realtime có cơ sở tốt hơn nhờ JR West running-position web service.

## 5. Mức Độ Đảm Bảo

- `Station`: cao.
- `Line`: cao.
- `Trip`: trung bình đến cao tùy prefecture/operator.
- `Realtime`: trung bình, không đồng đều giữa 3 prefecture.

## 6. Bước Tiếp Theo

1. Bắt đầu `trip/realtime` ở `Tokyo / Toei`.
2. Nếu cần `Miyazaki`, viết parser cho `JR Kyushu timetable`.
3. Nếu cần `Shimane`, đi sâu vào `JR West train-guide`.
