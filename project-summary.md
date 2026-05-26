# Tổng Hợp Project

Cập nhật: 2026-05-26

## Tổng Quan

Scope hiện tại gồm 3 prefecture: `Tokyo`, `Miyazaki`, `Shimane`.

Mục tiêu:

1. Lấy station catalog.
2. Vẽ railway lines và stations lên `Cesium`.
3. Lấy `trip` và `realtime train position` nếu nguồn dữ liệu cho phép.

## Quyết Định Dữ Liệu

- `Station catalog`: dùng `MLIT N02 + N03`.
- `Line để vẽ Cesium`: đang dùng `MLIT N02 RailroadSection`; có thể nâng cấp sang `ODPT GTFS/GTFS-JP` nếu cần geometry theo operator/trip.
- `Tokyo`: chốt `Toei`, dùng `ODPT GTFS/GTFS-JP`, `Train Location`, `GTFS-RT`.
- `Miyazaki`: `GTFS Data Repository` hiện không usable cho `pref=45`; ưu tiên `JR Kyushu timetable portal/PDF`.
- `Shimane`: tập trung `JR West`; trip static đi theo JR West timetable/route pages, không phải ODPT.

## Mức Độ Đảm Bảo

- `Station`: cao.
- `Line trên Cesium`: cao cho demo scope hiện tại.
- `Trip`: trung bình đến cao, phụ thuộc operator/source.
- `Realtime`: trung bình, không nên cam kết mọi operator đều có `real position`.

## Trạng Thái Hiện Tại

- Đã có station export JSON: [station_export_tool/stations_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/station_export_tool/stations_tokyo_miyazaki_shimane.json:1)
- Đã có line export JSON: [line_export_tool/rail_lines_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/line_export_tool/rail_lines_tokyo_miyazaki_shimane.json:1)
- Đã có Cesium viewer: [line_export_tool/viewer.html](/Users/account/Desktop/works/FPT/research/line_export_tool/viewer.html:1)
- Chưa có pipeline `trip/realtime` đã implement.
- Operator đầu tiên cho `Tokyo trip/realtime` là `Toei`.

## Ghi Chú Quan Trọng

- Kiểm tra ngày `2026-05-26`: `https://api.gtfs-data.jp/v2/files?pref=45` và `https://api.gtfs-data.jp/v2/feeds?pref=45` đều trả `[]`.
- `JR Kyushu / Miyazaki` chưa có GTFS public feed usable đã xác minh được.
- ODPT search ngày `2026-05-26` không tìm thấy `JR West` dataset cho `jrwest` hoặc `西日本旅客鉄道`.
- Bộ nguồn chính thức khả dụng nhất cho `JR Kyushu / Miyazaki`: timetable portal, station directory, route map, operation info, app / Train Navi.
