# Quyết Định Nguồn Dữ Liệu

Cập nhật: 2026-05-26

## 1. Station Catalog

Dùng `MLIT N02 + N03`.

Lý do:

- Bao phủ toàn quốc.
- Phù hợp với scope theo prefecture.
- Ổn định và chính thống.

## 2. Railway Lines Trên Cesium

Dùng `MLIT N02 RailroadSection` cho demo hiện tại.

Lý do:

- Vẽ line được ngay theo prefecture scope.
- Không cần chốt operator trước.

Có thể nâng cấp sau sang `ODPT GTFS/GTFS-JP` nếu cần geometry theo operator/trip chi tiết hơn.

## 3. Trip Và Realtime

- `Tokyo`: chốt `Toei`, dùng `ODPT GTFS/GTFS-JP`, `Train Location`, `GTFS-RT`.
- `Miyazaki`: `GTFS Data Repository` hoạt động nhưng trả rỗng cho `pref=45`; ưu tiên `JR Kyushu timetable portal/PDF` cho `trip static`.
- `Shimane`: tập trung `JR West`; dùng JR West timetable/route pages cho `trip static`, không dùng ODPT vì chưa tìm thấy dataset `jrwest` / `西日本旅客鉄道`.

## Rule Of Thumb

- Cần `ga theo prefecture` -> dùng `MLIT`.
- Cần `line demo nhanh` -> dùng `MLIT RailroadSection`.
- Cần `trip/realtime ở Tokyo` -> dùng `ODPT` và bắt đầu bằng `Toei`.
- Cần `trip ở Miyazaki` -> dùng `JR Kyushu timetable portal/PDF`.
- Cần `trip ở Shimane` -> dùng `JR West timetable/route pages`.

## Links Chính

- MLIT N02: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
- MLIT N03: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
- ODPT developer: https://developer.odpt.org/
- GTFS Data Repository docs: https://docs.gtfs-data.jp/api.v2.html
- JR Kyushu timetable portal: https://www.jrkyushu-timetable.jp/
- JR West timetable: https://www.westjr.co.jp/global/en/timetable/
- JR West train-guide: https://www.train-guide.westjr.co.jp/area_sanin.html
