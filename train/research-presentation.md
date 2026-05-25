# Research Presentation

Cap nhat: 2026-05-25

## 1. Bai toan can giai

Can xay dung demo railway cho scope hien tai gom 3 muc tieu:

1. Lay duoc danh sach tram/ga cua `Tokyo`, `Miyazaki`, `Shimane`
2. Ve duoc cac tuyen duong sat va tram/ga len `Cesium`
3. Lay duoc `trip` va thong tin `realtime` cua tau o muc du lieu nguon cho phep

## 2. Ket luan tong quan

Khong co 1 nguon duy nhat dap ung tot tat ca cac muc tieu.

Huong dung nhat la:

- `MLIT` cho station catalog va line nen theo prefecture
- `ODPT` cho trip, route, stop sequence, realtime train data
- `GTFS Data Repository` lam nguon bo sung rat quan trong cho `Miyazaki` va `Shimane`

## 3. Vi sao khong dung 1 nguon duy nhat

### Neu chi dung ODPT

Uu diem:

- Tot cho route/operator/realtime
- Co GTFS/GTFS-JP, GTFS-RT, Train Location, Train Status

Han che:

- Khong phai operator nao cung co coverage dong deu
- Khong phai prefecture nao cung thuan loi nhu Tokyo
- Miyazaki va Shimane khong nen duoc cam ket som ve trip/realtime neu chi dua tren ODPT

### Neu chi dung MLIT

Uu diem:

- Tot cho station catalog toan quoc
- Tot cho line nen va spatial filtering theo prefecture
- Chinh thong va de dung cho Cesium demo

Han che:

- Khong phu hop cho trip nghiep vu
- Khong phu hop cho realtime train position

## 4. Quyet dinh ve nguon du lieu

### 4.1 Station catalog

Dung:

- `MLIT N02`
- `MLIT N03`

Ly do:

- Lay duoc station catalog cua `Tokyo`, `Miyazaki`, `Shimane`
- Loc duoc ga theo prefecture
- Bao phu toan quoc va on dinh

### 4.2 Railway lines tren Cesium

Dang dung:

- `MLIT N02 RailroadSection`

Ly do:

- Ve duoc line ngay cho demo
- Khop voi scope `prefecture`
- Khong can chot operator truoc

Huong nang cap:

- `ODPT GTFS/GTFS-JP` neu can geometry theo operator/trip chi tiet hon

### 4.3 Trip va realtime

Dung:

- `ODPT GTFS / GTFS-JP`
- `ODPT Train Location`
- `ODPT GTFS-RT`
- `ODPT Train Status / Alert`

Ly do:

- Day la nhom du lieu nghiep vu van hanh tau
- Co the mo ta duoc:
  - trip
  - route
  - stop sequence
  - active trains
  - real position
  - estimated position

## 5. Danh gia theo tung khu vuc

### Tokyo

- `station`: cao
- `line`: cao
- `trip`: cao
- `realtime`: trung binh den cao tuy operator

Ket luan:

- Day la noi phu hop nhat de lam `ODPT trip + realtime`

### Miyazaki

- `station`: cao
- `line`: cao
- `trip`: trung binh
- `realtime`: thap den trung binh

Ket luan:

- Nen uu tien `station + line`
- `trip` can tim them nguon GTFS
- `realtime` khong nen cam ket o phase dau

### Shimane

- `station`: cao
- `line`: cao
- `trip`: trung binh
- `realtime`: thap den trung binh

Ket luan:

- Giong Miyazaki
- Nen uu tien `station + line`, sau do moi mo rong `trip`

## 6. Chien luoc multi-source

### Layer 1 - Spatial base

Nguon:

- `MLIT N02 + N03`

Dung de:

- station catalog
- line nen
- filter theo prefecture

### Layer 2 - Trip static

Nguon uu tien:

1. `ODPT GTFS / GTFS-JP`
2. `GTFS Data Repository`
3. Local GTFS feed

Dung de:

- `routes.txt`
- `trips.txt`
- `stop_times.txt`
- `stops.txt`
- `calendar.txt`
- `calendar_dates.txt`

### Layer 3 - Realtime

Nguon uu tien:

1. `ODPT Train Location`
2. `ODPT GTFS-RT`
3. `ODPT Train Status`
4. Local realtime feed

Dung de:

- active trains
- vi tri `real`
- vi tri `estimated`
- line status

## 7. Trip se di theo prefecture scope nhu the nao

Day la diem quan trong.

Nguon trip co the van la `operator-based`, nhung output cua he thong phai la `prefecture-based`.

Phuong an:

1. Lay `trip` tu GTFS
2. Lay `stop sequence` tu `stop_times.txt`
3. Join voi station catalog da normalize
4. Map tung stop vao `Tokyo`, `Miyazaki`, `Shimane`
5. Tu do cho phep filter/group trip theo `prefecture scope`

Y nghia:

- Nguon goc va scope hien thi khong nhat thiet giong nhau
- Backend la lop chuan hoa va noi 2 the gioi nay lai voi nhau

## 8. Muc do dam bao hien tai

### Co the cam ket kha chac chan

- Lay duoc danh sach ga cua `Tokyo`, `Miyazaki`, `Shimane`
- Ve duoc line va station tren `Cesium`

### Co the lam duoc nhung can chot operator/nguon truoc

- `trip`
- `route`
- `stop sequence`

### Khong nen cam ket tuyet doi

- Moi operator deu co realtime du
- Moi tau deu co GPS that

## 9. Artefacts da co

### Tai lieu

- [project-summary.md](/Users/account/Desktop/works/FPT/research/project-summary.md:1)
- [japan-rail-demo-goals.md](/Users/account/Desktop/works/FPT/research/japan-rail-demo-goals.md:1)
- [japan-rail-demo-plan.md](/Users/account/Desktop/works/FPT/research/japan-rail-demo-plan.md:1)
- [japan-rail-research-conclusions.md](/Users/account/Desktop/works/FPT/research/japan-rail-research-conclusions.md:1)
- [data-source-decision.md](/Users/account/Desktop/works/FPT/research/data-source-decision.md:1)
- [trip-source-strategy.md](/Users/account/Desktop/works/FPT/research/trip-source-strategy.md:1)

### Tool va data

- Station export:
  - [station_export_tool/export_japan_stations.py](/Users/account/Desktop/works/FPT/research/station_export_tool/export_japan_stations.py:1)
  - [station_export_tool/stations_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/station_export_tool/stations_tokyo_miyazaki_shimane.json:1)
- Rail line export:
  - [line_export_tool/export_japan_rail_lines.py](/Users/account/Desktop/works/FPT/research/line_export_tool/export_japan_rail_lines.py:1)
  - [line_export_tool/rail_lines_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/line_export_tool/rail_lines_tokyo_miyazaki_shimane.json:1)
  - [line_export_tool/viewer.html](/Users/account/Desktop/works/FPT/research/line_export_tool/viewer.html:1)

## 10. De xuat buoc tiep theo

### Buoc 1

Chot source strategy cho `trip`:

- Tokyo: `ODPT`
- Miyazaki: `GTFS Data Repository` hoac local GTFS neu ODPT khong phu hop
- Shimane: `GTFS Data Repository` hoac local GTFS neu ODPT khong phu hop

### Buoc 2

Thiet ke normalized model cho `trip`

### Buoc 3

Viet tool export `trip`

### Buoc 4

Moi tiep tuc sang `realtime`

## 11. Ket luan cuoi

Research hien tai da du ro de chot huong ky thuat:

- `MLIT` giai quyet tot bai toan station + line
- `ODPT` la huong chinh cho trip + realtime
- `GTFS Data Repository` la fallback quan trong cho Miyazaki va Shimane
- Kien truc dung phai la `multi-source normalization`

Do do, phuong an kha thi nhat hien tai la:

- demo `station + line` ngay
- tiep tuc xay `trip` theo prefecture scope
- mo rong `realtime` theo muc availability cua tung operator
