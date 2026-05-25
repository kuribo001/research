# Project Summary

Cap nhat: 2026-05-25

## Goal

Muc tieu hien tai cua project:

1. Lay duoc danh sach tram/ga cua `Tokyo`, `Miyazaki`, `Shimane`
2. Ve duoc cac tuyen duong sat va tram/ga len `Cesium`
3. Lay duoc `trip` va vi tri `realtime` cua tau o muc du lieu nguon cho phep

## Chon nguon du lieu

### 1. Danh sach tram/ga theo prefecture

Dung:

- `MLIT N02`
- `MLIT N03`

Ly do:

- Phu hop cho bai toan danh sach ga toan quoc
- Loc ga theo prefecture duoc
- Chinh thong, on dinh, pham vi bao phu rong

Ket qua:

- Co the lay danh sach ga cua `Tokyo`, `Miyazaki`, `Shimane`

### 2. Ve tuyen len Cesium

Dang dung:

- `MLIT N02 RailroadSection`

Huong nghien cuu tiep:

- `ODPT GTFS / GTFS-JP`

Ly do:

- `MLIT N02 RailroadSection` giup ve line duoc ngay cho demo
- `ODPT GTFS / GTFS-JP` phu hop hon neu muon geometry theo operator/trip/route chuan hon

Ket qua:

- Hien tai da ve duoc line len Cesium

### 3. Trip va realtime train position

Dung:

- `ODPT GTFS / GTFS-JP` cho `trip`, `route`, `stop sequence`
- `ODPT Train Location`
- `ODPT GTFS-RT`
- `ODPT Train Status / Alert`

Ly do:

- `ODPT` la nguon phu hop nhat cho bai toan nghiep vu tau dang hoat dong
- Du lieu goc co the theo `operator`, nhung backend se map ve `prefecture scope`
- Co the suy ra `real position`, `estimated position`, hoac `status only`

Ket qua mong doi:

- Co `trip` duoc nhom/loc theo `Tokyo`, `Miyazaki`, `Shimane`
- Co danh sach tau dang hoat dong
- Co vi tri tau o muc `real` hoac `estimated`

## Muc do dam bao

- `Danh sach ga`: `cao`
- `Ve line len Cesium`: `kha cao`
- `Trip`: `kha cao` neu operator co GTFS/GTFS-JP day du
- `Realtime vi tri tau`: `trung binh`, khong nen cam ket moi operator deu co GPS that

## Scope chot

### Phase 1

- Lay danh sach ga cua `Tokyo`, `Miyazaki`, `Shimane`
- Ve line va station tren `Cesium`

### Phase 2

- Lay `trip`, `route`, `stop sequence` tu `ODPT GTFS / GTFS-JP`
- Map `trip` tu nguon operator-based ve `prefecture scope`
- Chot operator scope cho du lieu nghiep vu, uu tien `Tokyo Toei` truoc

### Phase 3

- Lay `active trains + realtime/estimated position` tu `ODPT Train Location / GTFS-RT / Train Status`
- Map du lieu realtime ve `prefecture scope`
- Mo rong operator neu du lieu realtime du day

## Artefacts hien co

### Tai lieu

- [japan-rail-demo-goals.md](/Users/account/Desktop/works/FPT/research/japan-rail-demo-goals.md:1)
- [japan-rail-research-conclusions.md](/Users/account/Desktop/works/FPT/research/japan-rail-research-conclusions.md:1)
- [data-source-decision.md](/Users/account/Desktop/works/FPT/research/data-source-decision.md:1)
- [japan-rail-demo-plan.md](/Users/account/Desktop/works/FPT/research/japan-rail-demo-plan.md:1)

### Tool va data

- Station export script: [station_export_tool/export_japan_stations.py](/Users/account/Desktop/works/FPT/research/station_export_tool/export_japan_stations.py:1)
- Station JSON: [station_export_tool/stations_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/station_export_tool/stations_tokyo_miyazaki_shimane.json:1)
- Rail line export script: [line_export_tool/export_japan_rail_lines.py](/Users/account/Desktop/works/FPT/research/line_export_tool/export_japan_rail_lines.py:1)
- Rail line JSON: [line_export_tool/rail_lines_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/line_export_tool/rail_lines_tokyo_miyazaki_shimane.json:1)
- Cesium viewer: [line_export_tool/viewer.html](/Users/account/Desktop/works/FPT/research/line_export_tool/viewer.html:1)

## Ket luan

Huong hien tai la hop ly:

- `MLIT N02 + N03` cho station catalog theo prefecture
- `MLIT N02 RailroadSection` dang du de demo line tren Cesium
- `ODPT GTFS / GTFS-JP` la huong dung cho trip, sau do map ve `prefecture scope`
- `ODPT realtime datasets` la huong dung cho active trains va vi tri tau

Phan can quan ly ky nhat van la `realtime train position`, vi do day du lieu phu thuoc operator va khong the cam ket dong deu.
