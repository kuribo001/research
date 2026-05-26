# Data Source Decision

Cap nhat: 2026-05-26

## Decision

### 1. Station catalog

Dung `MLIT N02 + N03`.

Ly do:

- Bao phu toan quoc
- Phu hop voi scope theo prefecture
- On dinh va chinh thong

### 2. Railway lines tren Cesium

Dung `MLIT N02 RailroadSection` cho demo hien tai.

Ly do:

- Ve line duoc ngay theo prefecture scope
- Khong can chot operator truoc

Co the nang cap sau sang `ODPT GTFS/GTFS-JP` neu can geometry theo operator/trip chi tiet hon.

### 3. Trip va realtime

- `Tokyo`
  - chot `Toei`
  - dung `ODPT GTFS/GTFS-JP`, `Train Location`, `GTFS-RT`
- `Miyazaki`
  - `GTFS Data Repository` API hoat dong nhung tra rong cho `pref=45`
  - uu tien `JR Kyushu timetable portal/PDF` cho `trip static`
  - `realtime` moi xac minh duoc o muc web/app service, chua xac minh public machine-readable feed
- `Shimane`
  - uu tien `GTFS Data Repository` hoac local/operator source
  - rieng `JR West` co web running-position service dang chu y

## Rule of thumb

- Can `ga theo prefecture` -> dung `MLIT`
- Can `line demo nhanh` -> dung `MLIT RailroadSection`
- Can `trip/realtime o Tokyo` -> dung `ODPT` va bat dau bang `Toei`
- Can `trip o Miyazaki` -> dung `JR Kyushu timetable portal/PDF`

## Links chinh

- MLIT N02: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
- MLIT N03: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
- ODPT developer: https://developer.odpt.org/
- GTFS Data Repository docs: https://docs.gtfs-data.jp/api.v2.html
- JR Kyushu timetable portal: https://www.jrkyushu-timetable.jp/
- JR Kyushu operation info: https://www.jrkyushu.co.jp/trains/info/
