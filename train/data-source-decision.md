# Data Source Decision

Cap nhat: 2026-05-25

## Muc tieu

Chot cach chon nguon du lieu cho demo railway tai Nhat.

## Quyet dinh

### 1. Station catalog theo prefecture

Dung:

- `MLIT N02`
- `MLIT N03`

Ly do:

- Bao phu toan quoc
- Chinh thong
- Phu hop cho bai toan loc ga theo `Tokyo`, `Miyazaki`, `Shimane`

Ket qua:

- Lay duoc danh sach ga theo prefecture
- Co the loc theo ranh gioi hanh chinh

### 2. Railway lines de ve 3D

Dung:

- `MLIT N02 RailroadSection` cho demo dau
- `ODPT GTFS/GTFS-JP` cho geometry theo operator/trip chi tiet hon
- bo sung `ODPT Route information` neu can metadata

Ly do:

- `MLIT N02 RailroadSection` cho phep ve line duoc ngay tren Cesium theo prefecture
- `ODPT GTFS/GTFS-JP` phu hop hon cho bai toan route geometry theo operator
- Khi can mo rong sang trip/realtime theo operator, `ODPT` se dong bo hon

Ket qua:

- Ve duoc tuyen len Cesium
- Co duong nang cap sang geometry chi tiet hon neu sau nay can thay bang ODPT

### 3. Realtime trains

Dung:

- `ODPT GTFS/GTFS-JP` cho `trip`, `route`, `stop sequence`
- `ODPT Train Location`
- `ODPT GTFS-RT`
- `ODPT Train Status / Alert`

Ly do:

- Day la nhom du lieu nghiep vu van hanh tau
- ODPT phu hop hon MLIT cho realtime
- Mac du nguon goc theo `operator`, backend co the map ve `prefecture scope`

Ket qua:

- Lay duoc `trip` theo nguon operator, sau do group/filter theo `Tokyo`, `Miyazaki`, `Shimane`
- Lay duoc danh sach tau dang hoat dong
- Hien thi duoc vi tri `real` hoac `estimated`

## Muc do dam bao

- `MLIT N02 + N03`:
  - Muc do dam bao `cao` cho `station catalog theo prefecture`
- `MLIT N02 RailroadSection`:
  - Muc do dam bao `cao` cho `ve line len map 3D` o scope prefecture hien tai
- `ODPT GTFS/GTFS-JP`:
  - Muc do dam bao `kha cao` cho `geometry theo operator/trip`
- `ODPT Train Location / GTFS-RT / Train Status`:
  - Muc do dam bao `trung binh` cho `active trains + vi tri hien tai`
  - Khong nen cam ket moi operator deu co `real position`
  - Can support `estimated position` va `status_only`

## Rule of thumb

- Neu can `ga theo prefecture` -> dung `MLIT N02 + N03`
- Neu can `line demo nhanh theo prefecture` -> dung `MLIT N02 RailroadSection`
- Neu can `trip/operator/realtime` -> dung `ODPT`, roi normalize ve `prefecture scope`
- Backend nen normalize ca hai nhom nguon ve chung model du lieu

## Nguon chung minh

- MLIT official National Land Numerical Information:
  - https://www.mlit.go.jp/tochi_fudousan_kensetsugyo/chirikukannjoho/tochi_fudousan_kensetsugyo_tk17_000001_00028.html
- MLIT N02 railway data catalog:
  - https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
- MLIT N03 administrative boundary data catalog:
  - https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
- GTFS Realtime reference:
  - https://gtfs.org/documentation/realtime/reference/
- ODPT sample operator datasets:
  - Toei train location: https://ckan.odpt.org/dataset/r_train_location-toei
  - Yokohama train location: https://ckan.odpt.org/dataset/r_train_location-yokohamamunicipal
  - Tokyo Metro train status: https://ckan.odpt.org/en/dataset/r_train_status-tokyometro

## Nguon chinh

- MLIT N02 railway data: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
- MLIT N03 administrative boundary data: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
- ODPT developer site: https://developer.odpt.org/
