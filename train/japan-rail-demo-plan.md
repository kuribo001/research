# Japan Rail Demo Plan

Cap nhat: 2026-05-25

## 1. Muc tieu demo

Xay dung demo voi 3 muc tieu da chot:

1. Lay duoc danh sach cac tram/ga cua `Tokyo`, `Miyazaki`, `Shimane`.
2. Ve duoc cac tuyen duong sat va tram/ga len `Cesium`.
3. Lay duoc `trip` va thong tin `realtime` cua tau theo cac trang thai nghiep vu nhu:
   - dang dung o ga
   - dang di den ga tiep theo
   - vua roi ga truoc do

Y nghia cua 3 muc tieu:

- Muc tieu 1 tra loi bai toan `station catalog`.
- Muc tieu 2 tra loi bai toan `3D railway geometry`.
- Muc tieu 3 tra loi bai toan `live train operations`.

Stack:

- FE: ReactJS
- BE: Spring Boot
- AWS Lambda crawler: Java

## 2. Ket luan nghien cuu nhanh

### Nguon du lieu nen dung cho demo

Nen xem bo nguon hien tai thanh 2 nhom:

#### Nhom 1 - MLIT cho station catalog va line nen

- `MLIT N02` phu hop cho danh sach ga toan quoc
- `MLIT N03` phu hop cho loc theo prefecture
- `MLIT N02 RailroadSection` phu hop de ve line len Cesium cho demo dau

#### Nhom 2 - ODPT cho trip va realtime

- `ODPT` co du lieu route, GTFS/GTFS-JP, GTFS-RT, train location va train status cho nhieu operator
- Phu hop cho bai toan `trip`, `stop sequence`, `active trains`, `estimated/realtime position`

Ly do van can `ODPT`:

- Co du lieu ga, route, timetable, GTFS/GTFS-JP, GTFS-RT va train location cho nhieu operator.
- Co mot lop chuan hoa du lieu thay vi phai crawl tung website rieng.
- Co dataset phu hop cho demo o Tokyo va Yokohama.

Bo sung:

- Doi voi bai toan `lay danh sach ga theo prefecture`, can dung them `MLIT National Land Numerical Information`.
- Cu the:
  - `N02` cho du lieu ga/railway toan quoc
  - `N03` cho bien gioi hanh chinh de loc ga theo Tokyo, Miyazaki, Shimane va cac prefecture khac

### Khi nao dung ODPT, khi nao dung MLIT

Dung `ODPT` khi:

- Can data theo `operator`
- Can `GTFS/GTFS-JP`
- Can `route information`
- Can `station information` cho bai toan giao thong cong cong
- Can `realtime` nhu train location, train status, GTFS-RT

Dung `MLIT N02/N03` khi:

- Can `danh sach ga toan quoc`
- Can `loc ga theo prefecture`
- Can du lieu nen GIS chinh thong
- Can bao phu khu vuc ma ODPT khong phai lua chon tot

Khuyen nghi cho demo hien tai:

- `Station catalog theo prefecture`: dung `MLIT N02 + N03`
- `Railway line de ve 3D`: dung `MLIT N02 RailroadSection` cho demo dau; `ODPT GTFS/GTFS-JP` la huong nang cap khi can geometry theo operator
- `Realtime trains`: uu tien `ODPT Train Location / GTFS-RT`

### Luu y quan trong

- Muon dung API/key cua ODPT can dang ky user; site cua ODPT ghi ro can cho toi `2 business days` de duyet.
- Khong phai operator nao cung co `VehiclePosition` hoac GPS thuc su; nhieu noi chi co `Alert`, `Train status`, hoac `Train location` theo kieu tau dang o ga nao / dang o doan giua 2 ga.
- License giua cac operator khac nhau. Co operator dung `CC BY 4.0`, co operator dung `Public Transportation Open Data Basic License`, co operator chi mo trong `Challenge Limited License`.

## 3. Scope de xuat cho demo

### Scope Phase 1 - nen chot

Lam demo voi `3 prefecture` cho station va line:

1. `Tokyo`
2. `Miyazaki`
3. `Shimane`

Ly do:

- Giai quyet truc tiep goal `station catalog theo prefecture`
- Da co tool va du lieu MLIT de xuat station va line
- Cho phep demo Cesium ngay ca khi chua khoa xong scope realtime theo operator

### Scope Phase 2 - operator scope cho trip va realtime

Uu tien:

1. `Tokyo Toei`
2. `Yokohama Municipal Subway`

Ly do:

- Co du lieu station + route + static train info
- Co dataset `train location` rieng
- Phu hop cho demo nghiep vu tau dang hoat dong

Mo rong neu con thoi gian:

- `Tokyo Metro`: tot cho station/route/static GTFS, nhung realtime can kiem tra ky theo dataset thuc te
- `JR East`, `Tobu`, `Keikyu`: co the huu ich, nhung can review ky ve license va do day realtime

## 4. Nguon du lieu de xuat

### A. ODPT developer site

Cong dung:

- Dang ky API key
- Quan ly dieu kien su dung du lieu

Y nghia cho project:

- Day la cong viec can lam ngay tu ngay dau, vi neu cho duyet 2 business days thi no co the thanh critical path.

### B. Du lieu static de ve map 3D

Can ingest cac nhom du lieu sau:

- `Station information`
- `Route information`
- `Train information (GTFS/GTFS-JP)`
- `Station timetable` neu can hien thi chi tiet
- `MLIT N02 railway station data` neu can danh sach ga theo prefecture
- `MLIT N03 administrative boundary data` neu can loc theo prefecture

Dung de tao:

- Danh sach ga
- Thu tu ga tren tuyen
- Shapes/polyline cua tuyen
- Mapping route -> trip -> stop
- Geometry de ve tren map 3D

Ghi chu quan trong:

- Neu muc tieu la `ve duong sat len 3D`, nguon geometry nen uu tien la `GTFS shapes.txt`.
- `Route information` giup biet quan he nghiep vu giua line va station, nhung khong nen coi la nguon geometry chinh neu da co `shapes.txt`.
- Neu muc tieu la `lay danh sach ga theo prefecture`, huong thuc te va on dinh hon la `MLIT N02 + N03`, khong phai ODPT.

### C. Du lieu realtime

Can chia thanh 2 nhom:

1. `Train location information JSON`
2. `GTFS-RT` neu operator co feed realtime day du

Dung de tao:

- Danh sach tau dang chay
- Trang thai tau
- Vi tri hien tai cua tau
- Delay / alert neu co

## 5. Kien truc de xuat

### Tong the

`ODPT / MLIT feeds -> Lambda crawler -> S3 raw zone -> Parser/Normalizer -> PostgreSQL(PostGIS) -> Spring Boot API -> React map UI`

### Vi sao nen co 2 lop raw + normalized

- Raw zone giup debug feed loi, doi schema, doi license.
- Lop normalized giup FE/BE khong bi phu thuoc truc tiep vao schema cua tung operator.

### Dich vu

#### 1. AWS Lambda crawler (Java)

Nhiem vu:

- Lay static GTFS/JSON theo lich.
- Lay MLIT N02/N03 neu can bo sung station catalog theo prefecture.
- Lay realtime train location / GTFS-RT theo chu ky ngan.
- Ghi raw file vao S3.
- Parse va upsert vao DB.
- Ghi log job, so record, checksum, error.

Lich goi y:

- Static GTFS/route/station: 1 lan/ngay
- MLIT N02/N03: 1 lan/ngay hoac khi can refresh dataset
- Train location: moi 15-30 giay cho demo
- Alerts/status: moi 30-60 giay

#### 2. Spring Boot backend

Nhiem vu:

- Cung cap REST API cho FE
- Gom va chuan hoa du lieu nhieu operator
- Expose endpoint realtime
- Cache ket qua hot queries

Khuyen nghi:

- Batch insert/upsert
- Redis optional neu can
- SSE/WebSocket de phase 2; phase 1 co the cho FE polling

#### 3. React frontend

Nhiem vu:

- Hien thi map 3D
- Ve route polyline
- Ve station markers
- Ve train markers dang di chuyen
- Panel ben phai cho operator, route, train, station

Khuyen nghi:

- `CesiumJS`
- Filter theo city/operator/route
- Refresh 15-30 giay

## 6. Mo hinh du lieu normalized

### Bang static

- `operators`
- `railways`
- `stations`
- `prefectures`
- `station_groups`
- `railway_stations`
- `shapes`
- `trips`
- `stop_times`
- `service_calendars`

### Bang realtime

- `train_positions`
- `train_statuses`
- `service_alerts`
- `realtime_snapshots`
- `crawler_job_logs`

### Truong quan trong cho tracking

`train_positions`

- operator_id
- railway_id
- train_id
- trip_id
- train_number
- direction
- current_station_id
- next_station_id
- from_station_id
- to_station_id
- latitude
- longitude
- delay_seconds
- status
- observed_at
- raw_payload_ref

Ghi chu:

- `latitude/longitude` khong nen bat buoc o layer normalized.
- Neu feed chi cho biet tau dang o giua `from_station` va `to_station` thi BE se noi suy marker len line shape de FE van ve duoc vi tri.

## 7. Cach xu ly "vi tri hien tai"

Can tach thanh 3 muc:

### Muc A - tot nhat

Feed co `VehiclePosition` hoac co lat/lng ro rang.

Ket qua:

- FE ve marker dung vi tri feed.

### Muc B - thuc te nhat cho nhieu operator

Feed chi co:

- ga hien tai
- ga ke tiep
- hoac doan `from_station -> to_station`

Ket qua:

- Backend noi suy vi tri marker tren polyline cua route.
- UI phai gan nhan la `estimated position`.
- Van co the suy ra duoc cac trang thai nghiep vu:
  - `stopped_at_station`
  - `moving_to_next_station`
  - `departed_previous_station`

### Muc C - toi thieu

Feed chi co `alert/status`, khong co location.

Ket qua:

- Hien thi danh sach tau/line dang gap su co
- Khong dua vao scope "moving train map"

## 8. Phuong an giai quyet theo tung muc tieu

### Muc tieu 1 - Lay thong tin cac tram/ga

Du lieu nguon:

- `Station information`
- `GTFS stops.txt`
- `MLIT N02 railway station data`
- `MLIT N03 administrative boundary data`

Can lay:

- station_id
- station_code neu co
- ten ga
- operator
- tuyen thuoc ve
- toa do lat/lng
- thu tu tren tuyen
- ga truoc / ga sau
- prefecture cua ga

Phuong an:

1. Neu bai toan la demo theo operator/tuyen o Tokyo, uu tien `ODPT + GTFS`.
2. Neu bai toan la danh sach ga theo prefecture nhu Tokyo, Miyazaki, Shimane, uu tien `MLIT N02 + N03`.
3. Backend normalize ca 2 nhom nguon ve model chung `stations`, gan them thong tin `source_type` neu can truy vet.

Ket qua nghiep vu:

- Tra cuu danh sach ga theo city/operator/line
- Tra cuu danh sach ga theo prefecture
- Ve marker ga tren map
- Hien thi popup chi tiet ga

### Muc tieu 2 - Lay thong tin cac tuyen duong sat de ve 3D

Du lieu nguon:

- `GTFS shapes.txt`
- `GTFS trips.txt`
- `GTFS routes.txt`
- `Route information`
- `Station information`

Phuong an:

1. Lay `shapes.txt` lam geometry chinh.
2. Join `trips.txt` de biet shape nao thuoc route nao.
3. Join `routes.txt` va `Route information` de lay ten tuyen, operator, huong di chuyen.
4. Normalize geometry thanh chuoi toa do `[lon, lat, height?]`.
5. FE Cesium doc geometry nay de ve polyline tren ban do 3D.

Ket qua nghiep vu:

- Ve duoc tuyen duong sat len Cesium.
- Focus theo tung operator/line.
- Click vao line de mo thong tin tuyen.

Luu y:

- Giai doan dau co the dat `height = 0` va ve polyline tren mat dat.
- Neu muon 3D dep hon, phase sau moi can nghien cuu them terrain clamping, offset, animation camera.

### Muc tieu 3 - Lay danh sach tau dang hoat dong va vi tri hien tai

Du lieu nguon:

- `GTFS/GTFS-JP` cho `trip`, `route`, `stop sequence`
- `Train location information`
- `GTFS-RT VehiclePosition` neu operator co
- `Train status / Alert` de bo sung nghia nghiep vu

Can chuan hoa thanh 1 model chung:

- trip_id
- train_id
- train_number
- operator
- railway
- direction
- current_station
- next_station
- previous_station
- from_station
- to_station
- latitude
- longitude
- position_type: `real` | `estimated`
- operation_status
- observed_at
- prefecture_scope

Phuong an suy ra trang thai:

- Neu co `current_station` va tau dang dung: `stopped_at_station`
- Neu co `from_station` + `to_station`: `moving_between_stations`
- Neu co `previous_station` + `next_station`: `departed_previous_station`
- Neu chi co alert/status ma khong co location: chi hien line status, khong ve moving marker

Phuong an map ve `prefecture scope`:

1. Lay `trip`, `route`, `stop sequence` tu `ODPT GTFS / GTFS-JP`.
2. Join voi station data de xac dinh `trip` nay di qua nhung ga nao.
3. Map cac ga do ve `Tokyo`, `Miyazaki`, `Shimane` dua tren station catalog da co.
4. Tu do cho phep filter/nhom `trip` va `active trains` theo `prefecture scope`, du nguon goc van la `operator-based`.

Ket qua nghiep vu:

- Danh sach `trip` theo prefecture scope
- Danh sach tau dang hoat dong theo line
- Marker tau tren map
- Trang thai tau de user hieu no dang dung, dang den, hay vua roi ga
## 9. API backend de xuat

### Static APIs

- `GET /api/operators`
- `GET /api/cities`
- `GET /api/railways?operator=...&city=...`
- `GET /api/stations?railwayId=...`
- `GET /api/map/routes?operator=...`
- `GET /api/map/routes/3d?operator=...&railwayId=...`
- `GET /api/stations/{stationId}`

### Realtime APIs

- `GET /api/realtime/trains?operator=...&railwayId=...`
- `GET /api/realtime/trains/{trainId}`
- `GET /api/realtime/trains/active?operator=...`
- `GET /api/realtime/alerts?operator=...`
- `GET /api/realtime/snapshot?operator=...`

### Admin/health

- `GET /api/health`
- `GET /api/admin/crawler-jobs`
- `POST /api/admin/reimport/static`

## 10. Luong hien thi FE

### Man hinh 1 - Network map

- Chon operator/city
- Ve toan bo route
- Hien tat ca stations
- Click route de focus
- Ho tro ve 3D tren Cesium

### Man hinh 2 - Live trains

- Marker tau dang chay
- Mau marker theo operator/line
- Tooltip hien:
  - train number
  - current station
  - next station
  - operation status
  - position type
  - updated at

### Man hinh 3 - Station detail

- Ten ga
- Operator
- Thuoc tuyen nao
- Danh sach ga truoc/sau
- Realtime trains lien quan

## 11. Ke hoach thuc hien demo

### Giai doan 0 - Khoi dong (0.5-1 ngay)

- Dang ky ODPT account/API key
- Chot scope Phase 1: `Tokyo`, `Miyazaki`, `Shimane` cho station + line
- Chot scope Phase 2: `Toei` truoc cho trip + realtime
- Chot DB: PostgreSQL + PostGIS
- Tao repo structure: `frontend/`, `backend/`, `crawler/`, `docs/`

### Giai doan 1 - Nghien cuu va spike du lieu (1-2 ngay)

- Download va doc 1 bo static GTFS/JSON cua Toei
- Download va doc 1 bo train location cua Toei
- Download va doc `MLIT N02/N03` cho bai toan station list theo prefecture
- Mapping field raw -> normalized
- Xac dinh feed nao co lat/lng that, feed nao chi co segment/station
- Xac dinh du lieu nao du de ve tuyen len Cesium 3D
- Viet tai lieu field mapping

Deliverable:

- 1 file mapping schema
- 1 sample JSON normalized
- 1 sample route geometry cho Cesium
- 1 sample station list theo prefecture

### Giai doan 2 - Static data pipeline (2 ngay)

- Viet Lambda ingest static data
- Parse station/route/shape/trip/stop_times
- Parse MLIT N02/N03 neu can station catalog theo prefecture
- Upsert vao DB
- Tao API static trong Spring Boot

Deliverable:

- FE goi API va ve duoc route 3D + station tren map
- API lay duoc station list theo prefecture

### Giai doan 3 - Realtime pipeline (2 ngay)

- Viet Lambda ingest train location / alert
- Luu snapshot raw vao S3
- Normalize train position/status vao DB
- Tao API realtime

Deliverable:

- FE ve duoc tau dang hoat dong, refresh theo chu ky, va hien duoc trang thai nghiep vu

### Giai doan 4 - UI demo (1-2 ngay)

- Filter theo operator/line
- Popup/side panel chi tiet
- Badge cho `real position` vs `estimated position`
- Xu ly loading/error/empty states

Deliverable:

- Ban demo co the thuyet trinh

### Giai doan 5 - Hardening (1 ngay)

- Log crawler
- Retry / DLQ neu can
- Throttle / timeout
- Snapshot regression data
- Demo script

## 12. Risks va cach giam rui ro

### Rui ro 1 - Cham API key ODPT

Giam rui ro:

- Dang ky ngay.
- Song song chuan bi schema, DB, FE mock data.

### Rui ro 2 - License khong phu hop cho demo mo rong

Giam rui ro:

- Phase 1 uu tien station + line cho `Tokyo`, `Miyazaki`, `Shimane`.
- Phase 2 moi mo rong `Toei` va `Yokohama` cho trip/realtime.
- Operator nao dung `Challenge Limited License` thi de sang phase 2.

### Rui ro 3 - Khong co GPS chinh xac

Giam rui ro:

- Hien thi ro `estimated position`.
- Dung shape + from/to station de noi suy.

### Rui ro 4 - Schema moi operator khac nhau

Giam rui ro:

- Dung adapter pattern trong crawler.
- Chia `raw model` va `normalized model`.

### Rui ro 5 - Realtime feed co luc trong/tre

Giam rui ro:

- Luu `observed_at`
- Hien thi do tre du lieu tren FE
- Co fallback sang status/alert

## 13. De xuat output demo cuoi cung

### Bat buoc

- Lay duoc station list theo prefecture cho Tokyo, Miyazaki, Shimane
- Map 3D route + station tren Cesium cho Tokyo, Miyazaki, Shimane
- Realtime train markers cho cac operator co location feed
- Filter operator/line
- API tai lieu hoa ro
- Trang thai tau o muc nghiep vu: dang dung, dang den ga tiep theo, vua roi ga

### Nen co

- Timeline update/last refresh
- Panel alert/service status
- Lich su 5-10 phut gan nhat cua train position

### Chua can trong demo dau

- Route planning cho end-user
- Multi-language full production
- Du doan ETA phuc tap
- Cover all Japan

## 14. Thu tu uu tien implementation

1. MLIT station list cho Tokyo, Miyazaki, Shimane
2. MLIT rail lines cho Cesium
3. ODPT account + key
4. Static GTFS/route/station cua Toei
5. Realtime train location cua Toei
6. FE map cho 1 operator + line + station
7. Yokohama operator
8. Optional: Tokyo Metro static + alert

## 15. Kien nghi cuoi

Neu muc tieu la `ra demo nhanh, ve duoc Cesium, co station catalog theo prefecture, va tien toi realtime`, scope hop ly nhat la:

- `MLIT N02 + N03` cho station catalog cua `Tokyo`, `Miyazaki`, `Shimane`
- `MLIT N02 RailroadSection` cho line demo tren Cesium
- `Tokyo Toei` lam operator ODPT chinh cho phase `trip + realtime`
- `Yokohama Municipal Subway` la operator thu hai neu can mo rong

Khong nen bat dau bang `all Japan` hoac `JR East full coverage`, vi rui ro lon nhat khong nam o code ma nam o:

- license
- do day cua realtime feed
- thoi gian chuan hoa du lieu giua cac operator

## 16. Nguon tham khao chinh

- ODPT developer site: https://developer.odpt.org/
- MLIT railway data catalog N02: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
- MLIT administrative boundary catalog N03: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
- MLIT railway data file used for station export: https://nlftp.mlit.go.jp/ksj/gml/data/N02/N02-24/N02-24_GML.zip
- MLIT prefecture boundary files used for station export:
  - Tokyo: https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_13_GML.zip
  - Shimane: https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_32_GML.zip
  - Miyazaki: https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_45_GML.zip
- MLIT GTFS-JP materials: https://www.mlit.go.jp/sogoseisaku/transport/sosei_transport_tk_000067.html
- Toei train information (GTFS/GTFS-JP): https://ckan.odpt.org/dataset/train-toei
- Toei route information: https://ckan.odpt.org/dataset/r_route-toei
- Toei station information: https://ckan.odpt.org/organization/toei?organization=toei
- Toei train location: https://ckan.odpt.org/dataset/r_train_location-toei
- Toei GTFS-RT realtime: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei
- Yokohama train information (GTFS/GTFS-JP): https://ckan.odpt.org/dataset/yokohama_municipal_train
- Yokohama route information: https://ckan.odpt.org/en/dataset/r_route-yokohamamunicipal
- Yokohama station information: https://ckan.odpt.org/dataset/r_station-yokohamamunicipal
- Yokohama train location: https://ckan.odpt.org/dataset/r_train_location-yokohamamunicipal
- Yokohama GTFS-RT realtime: https://ckan.odpt.org/dataset/r_train_gtfs_rt-yokohamamunicipal
- Tokyo Metro train information (GTFS/GTFS-JP): https://ckan.odpt.org/dataset/train-tokyometro
- Tokyo Metro route information: https://ckan.odpt.org/dataset/r_route-tokyometro
- Tokyo Metro station information: https://ckan.odpt.org/dataset/r_station-tokyometro
- Tokyo Metro train status: https://ckan.odpt.org/en/dataset/r_train_status-tokyometro
- Tokyo Metro GTFS-RT realtime: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-tokyometro
- JR East station information: https://ckan.odpt.org/dataset/jreast__r_station
- JR East route information: https://ckan.odpt.org/dataset/jreast__r_route
- JR East train location: https://ckan.odpt.org/dataset/jreast__r_train_location
- Tobu train location: https://ckan.odpt.org/dataset/tobu__r_train_location
- Keikyu train location: https://ckan.odpt.org/dataset/keikyu__r_train_location
- GTFS realtime vehicle positions reference: https://gtfs-llm-translation.odpt.org/documentation/realtime/feed-entities/vehicle-positions/
