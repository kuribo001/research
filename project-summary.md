# Project Summary

Cap nhat: 2026-05-26

## Tong quan

Scope hien tai gom 3 prefecture: `Tokyo`, `Miyazaki`, `Shimane`.

Muc tieu:

1. Lay station catalog
2. Ve railway lines va stations len `Cesium`
3. Lay `trip` va `realtime train position` neu nguon du lieu cho phep

## Quyet dinh du lieu

- `Station catalog`
  - dung `MLIT N02 + N03`
- `Line de ve Cesium`
  - dang dung `MLIT N02 RailroadSection`
  - co the nang cap sang `ODPT GTFS/GTFS-JP` neu can geometry theo operator/trip
- `Trip va realtime`
  - `Tokyo`: chot `Toei` voi `ODPT GTFS/GTFS-JP`, `Train Location`, `GTFS-RT`
  - `Miyazaki`: `GTFS Data Repository` hien khong usable cho `pref=45`; uu tien `JR Kyushu timetable portal/PDF`
  - `Shimane`: uu tien `GTFS Data Repository` hoac local/operator source, tuy operator

## Muc do dam bao

- `Station`: cao
- `Line tren Cesium`: cao cho demo scope hien tai
- `Trip`: trung binh den cao, phu thuoc operator/source
- `Realtime`: trung binh, khong nen cam ket moi operator deu co `real position`

## Trang thai hien tai

- Da co station export JSON:
  - [station_export_tool/stations_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/station_export_tool/stations_tokyo_miyazaki_shimane.json:1)
- Da co line export JSON va Cesium viewer:
  - [line_export_tool/rail_lines_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/line_export_tool/rail_lines_tokyo_miyazaki_shimane.json:1)
  - [line_export_tool/viewer.html](/Users/account/Desktop/works/FPT/research/line_export_tool/viewer.html:1)
- Chua co pipeline `trip/realtime` da implement
- Operator dau tien da duoc chot cho `Tokyo trip/realtime` la `Toei`

## Ghi chu quan trong

- Kiem tra ngay `2026-05-26`:
  - `https://api.gtfs-data.jp/v2/files?pref=45` -> `[]`
  - `https://api.gtfs-data.jp/v2/feeds?pref=45` -> `[]`
- Vi vay, `JR Kyushu / Miyazaki` hien chua co GTFS public feed usable da xac minh duoc trong research nay
- Sau khi mo rong tim kiem, bo nguon chinh thuc kha dung nhat cho `JR Kyushu / Miyazaki` hien la:
  - timetable portal: https://www.jrkyushu-timetable.jp/
  - station directory: https://www.jrkyushu.co.jp/railway/station/
  - route map: https://www.jrkyushu.co.jp/routemap/index.jsp
  - operation info: https://www.jrkyushu.co.jp/trains/info/
  - app / Train Navi: https://www.jrkyushu.co.jp/app/lp/
