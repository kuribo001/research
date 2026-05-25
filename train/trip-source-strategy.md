# Trip Source Strategy

Cap nhat: 2026-05-25

## Muc tieu

Chot chien luoc lay du lieu `trip` va `realtime train position` cho scope hien tai:

- `Tokyo`
- `Miyazaki`
- `Shimane`

Huong tiep can:

- Scope hien thi va nghiep vu cua app se di theo `prefecture`
- Nguon du lieu goc co the den tu nhieu he thong khac nhau
- Backend se normalize va map ve `prefecture scope`

## Nguyen tac chon nguon

### 1. Nguon nen khong gian

Dung:

- `MLIT N02`
- `MLIT N03`

Vai tro:

- Lay station catalog
- Lay line nen
- Xac dinh ga/line thuoc `Tokyo`, `Miyazaki`, `Shimane`

Khong dung cho:

- `trip`
- `realtime train position`

### 2. Nguon nghiep vu static cho trip

Uu tien:

1. `ODPT GTFS / GTFS-JP`
2. `GTFS Data Repository`
3. Nguon GTFS local cua operator/prefecture neu co

Vai tro:

- `routes.txt`
- `trips.txt`
- `stop_times.txt`
- `stops.txt`
- `calendar.txt`, `calendar_dates.txt`
- `shapes.txt` neu can geometry theo trip/route

Link tham khao:

- ODPT developer site:
  - https://developer.odpt.org/
- Toei GTFS/GTFS-JP dataset:
  - https://ckan.odpt.org/ja/dataset/train-toei
- Tokyo Metro GTFS/GTFS-JP dataset:
  - https://ckan.odpt.org/dataset/train-tokyometro
- GTFS Data Repository:
  - https://gtfs-data.jp/
- GTFS Data Repository API:
  - https://docs.gtfs-data.jp/api.v2.html
- CKAN API docs:
  - https://docs.ckan.org/en/latest/api.html
- GTFS static reference:
  - https://gtfs.org/documentation/schedule/reference/

Cach lay:

### Cach 1 - Lay tu ODPT GTFS/GTFS-JP

1. Xac dinh dataset operator can dung tren CKAN ODPT.
   Vi du:
   - Toei: `train-toei`
   - Tokyo Metro: `train-tokyometro`
2. Lay metadata dataset qua CKAN Action API `package_show`.
   Vi du:
   - `https://ckan.odpt.org/api/3/action/package_show?id=train-toei`
   - `https://ckan.odpt.org/api/3/action/package_show?id=train-tokyometro`
3. Tu response metadata, lay `resources[].url` cua resource GTFS/GTFS-JP.
4. Download file zip GTFS.
5. Parse cac file:
   - `routes.txt`
   - `trips.txt`
   - `stop_times.txt`
   - `stops.txt`
   - `calendar.txt`
   - `calendar_dates.txt`
   - `shapes.txt` neu can

Khi nao dung:

- Uu tien cho `Tokyo`
- Uu tien khi can dong bo cung mot he sinh thai voi `ODPT realtime`
- Uu tien khi can route/trip semantics theo operator

### Cach 2 - Lay tu GTFS Data Repository

1. Tim feed theo prefecture bang API `GET /files` hoac `GET /feeds`.
2. Dung query `pref` de loc theo prefecture code.
   Vi du:
   - Tokyo: `pref=13`
   - Shimane: `pref=32`
   - Miyazaki: `pref=45`
3. Lay trong response cac truong:
   - `organization_id`
   - `feed_id`
   - `file_url`
   - `feed_page_url`
   - `real_time.*` neu co
4. Download `file_url` de lay GTFS zip.
5. Parse bo GTFS giong cach 1.

Vi du API:

- Tim GTFS files theo prefecture:
  - `https://api.gtfs-data.jp/v2/files?pref=13`
  - `https://api.gtfs-data.jp/v2/files?pref=32`
  - `https://api.gtfs-data.jp/v2/files?pref=45`
- Liet ke feeds theo prefecture:
  - `https://api.gtfs-data.jp/v2/feeds?pref=13`

Khi nao dung:

- Uu tien cho `Miyazaki`
- Uu tien cho `Shimane`
- Dung lam fallback neu `ODPT` khong co coverage static GTFS phu hop

### Cach 3 - Lay tu local GTFS feed cua operator/prefecture

1. Tim open data page cua operator hoac prefecture.
2. Xac minh license va do moi du lieu.
3. Download GTFS zip hoac resource zip cong bo.
4. Parse cung mot pipeline nhu hai cach tren.

Khi nao dung:

- Khi `ODPT` va `GTFS Data Repository` khong du phu hop
- Khi can bo sung cho `Miyazaki` / `Shimane`

### 3. Nguon realtime

Uu tien:

1. `ODPT Train Location`
2. `ODPT GTFS-RT`
3. `ODPT Train Status / Alert`
4. Realtime feed local cua operator neu co

Vai tro:

- Lay danh sach tau dang hoat dong
- Lay vi tri `real` neu co
- Noi suy `estimated position` neu khong co GPS
- Fallback sang `status only` neu khong co location

## Danh gia theo tung prefecture

### Tokyo

Static trip source:

- `ODPT GTFS / GTFS-JP`: rat phu hop
- `GTFS Data Repository`: co the dung bo sung neu can

Realtime source:

- `ODPT Train Location / GTFS-RT / Status`: phu hop nhat

Danh gia:

- `station`: cao
- `line`: cao
- `trip`: cao
- `realtime`: trung binh den cao tuy operator

Khuyen nghi:

- Chon `Tokyo Toei` lam operator dau tien cho phase trip/realtime

### Miyazaki

Static trip source:

- `ODPT`: chua nen cam ket
- `GTFS Data Repository`: la ung vien rat quan trong
- Local operator/public GTFS: can dieu tra them

Realtime source:

- `ODPT`: chua nen cam ket
- Local operator realtime neu co: can dieu tra

Danh gia:

- `station`: cao
- `line`: cao
- `trip`: trung binh, phu thuoc availability cua GTFS
- `realtime`: thap den trung binh

Khuyen nghi:

- Uu tien muc tieu `trip static` truoc
- Khong cam ket `realtime position` cho Miyazaki o phase dau

### Shimane

Static trip source:

- `ODPT`: chua nen cam ket
- `GTFS Data Repository`: la ung vien rat quan trong
- Local operator/public GTFS: can dieu tra them

Realtime source:

- `ODPT`: chua nen cam ket
- Local operator realtime neu co: can dieu tra

Danh gia:

- `station`: cao
- `line`: cao
- `trip`: trung binh, phu thuoc availability cua GTFS
- `realtime`: thap den trung binh

Khuyen nghi:

- Uu tien muc tieu `trip static` truoc
- Khong cam ket `realtime position` cho Shimane o phase dau

## Chien luoc multi-source

### Layer 1 - Spatial base

- `MLIT N02 + N03`

Dung de:

- xac dinh station theo prefecture
- xac dinh line theo prefecture
- map station/line vao `Tokyo`, `Miyazaki`, `Shimane`

### Layer 2 - Trip static

Uu tien nguon:

1. `ODPT GTFS / GTFS-JP`
2. `GTFS Data Repository`
3. Local GTFS feed

Dung de:

- lay `trip`
- lay `route`
- lay `stop sequence`
- lay service calendar

### Layer 3 - Realtime

Uu tien nguon:

1. `ODPT Train Location`
2. `ODPT GTFS-RT`
3. `ODPT Train Status`
4. Local realtime feed

Dung de:

- active trains
- current position
- estimated position
- line status

## Normalized output model cho trip

Toi thieu nen co:

- `prefecture_scope`
- `source_type`
- `operator_id`
- `route_id`
- `route_name`
- `trip_id`
- `trip_headsign`
- `service_id`
- `direction_id`
- `shape_id`
- `stop_sequence`
- `stops`
- `origin_station_id`
- `destination_station_id`

## Cach map trip ve prefecture scope

1. Lay `trip` tu GTFS.
2. Lay danh sach `stops` cua trip tu `stop_times.txt`.
3. Join voi station catalog da normalize.
4. Gan moi stop vao `Tokyo`, `Miyazaki`, `Shimane` neu no nam trong scope.
5. Xac dinh `trip` thuoc prefecture nao theo tap stop cua no.
6. Neu 1 trip di qua nhieu prefecture:
   - co the duplicate view theo tung prefecture
   - hoac gan `multi_prefecture = true`

Khuyen nghi phase dau:

- Uu tien logic `trip co stop thuoc prefecture nao thi hien trong prefecture do`

## Fallback strategy

### Neu Tokyo

- Uu tien `ODPT`
- Neu thieu 1 feed, tim GTFS static tu source khac

### Neu Miyazaki / Shimane

- Neu khong co `ODPT` phu hop:
  - tim `GTFS Data Repository`
  - tim feed local
- Neu khong co realtime:
  - van support `trip static`
  - khong bat buoc support `live position`

## Ket luan

Huong dung nhat hien tai:

- `MLIT` cho station va line theo prefecture
- `ODPT` cho trip/realtime khi co coverage tot
- `GTFS Data Repository` la nguon bo sung quan trong cho `Miyazaki` va `Shimane`
- Kien truc backend phai la `multi-source normalization`, khong duoc khoa cung vao 1 nguon

## Nguon tham khao

- MLIT N02 railway data: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
- MLIT N03 administrative boundary data: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
- ODPT developer site: https://developer.odpt.org/
- Toei GTFS/GTFS-JP: https://ckan.odpt.org/dataset/train-toei
- Toei train location: https://ckan.odpt.org/dataset/r_train_location-toei
- Toei GTFS-RT: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei
- GTFS Data Repository: https://gtfs-data.jp/
- GTFS Data Repository API docs: https://docs.gtfs-data.jp/api.v2.html
- GTFS static reference: https://gtfs.org/documentation/schedule/reference/
- GTFS realtime reference: https://gtfs.org/documentation/realtime/reference/
