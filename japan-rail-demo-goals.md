# Mục Tiêu Demo Japan Rail

Cập nhật: 2026-05-26

## Mục Tiêu

1. Lấy danh sách trạm/ga của `Tokyo`, `Miyazaki`, `Shimane`.
2. Vẽ các tuyến đường sắt và trạm/ga lên `Cesium`.
3. Lấy `trip` và thông tin `realtime` của tàu ở mức dữ liệu nguồn cho phép.

## Kết Quả Cần Đạt

- `Station`: có danh sách ga, tên ga, tọa độ, prefecture, line/operator liên quan.
- `Line`: có geometry để vẽ polyline trên Cesium và hiển thị line + station cùng lúc.
- `Trip và realtime`: có `trip`, `route`, `stop sequence`, active trains, và trạng thái `real position`, `estimated position`, hoặc `status only`.

## In Scope

- Station catalog theo `Tokyo`, `Miyazaki`, `Shimane`.
- Railway lines và stations trên map 3D.
- Trip và realtime trong phạm vi nguồn dữ liệu khả dụng.

## Out Of Scope

- Cover toàn bộ Nhật Bản.
- Route planning cho end-user.
- ETA/prediction phức tạp.
- Cam kết GPS thật cho mọi operator.

## Ghi Chú

- `MLIT N02 + N03` dùng cho station catalog theo prefecture.
- `MLIT N02 RailroadSection` đang dùng để vẽ line demo.
- `ODPT` là hướng ưu tiên cho `trip/realtime` ở `Tokyo`.
- `Toei` là operator đã được chốt cho `Tokyo`.
- `Miyazaki` và `Shimane` có thể cần timetable/local source thay vì GTFS/GTFS-RT công khai.
