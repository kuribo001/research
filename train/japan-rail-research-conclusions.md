# Japan Rail Research Conclusions

Cap nhat: 2026-05-25

## Ket luan nghien cuu nhanh

### 1. Nguon du lieu nen dung

Nguon du lieu nen uu tien la `ODPT - Public Transportation Open Data Center`.

Ly do:

- Co du lieu ga, route, timetable, GTFS/GTFS-JP, GTFS-RT va train location cho nhieu operator.
- Co mot lop chuan hoa du lieu tot hon so voi viec crawl thu cong tung website/operator.
- Phu hop de lam demo cho mot so khu vuc nhu Tokyo va Yokohama.

Nguon du lieu bo sung can ghi ro la `MLIT National Land Numerical Information`.

Ly do:

- Phu hop hon cho bai toan `danh sach ga toan quoc` hoac `loc ga theo prefecture`.
- Khong bi gioi han theo do phu operator nhu ODPT.
- Co san du lieu nen `railway stations` va `administrative boundaries` de tu loc ga theo Tokyo, Miyazaki, Shimane va cac prefecture khac.

Ket luan su dung nguon:

- `ODPT`: uu tien cho demo nghiep vu giao thong, route/operator data, GTFS/GTFS-RT, realtime.
- `MLIT N02/N03`: uu tien cho bai toan lay danh sach ga theo prefecture, khong gian hanh chinh, va du lieu nen toan quoc.

### 1.1 Khi nao dung ODPT, khi nao dung MLIT

Dung `ODPT` khi:

- Can du lieu theo `operator`
- Can `GTFS/GTFS-JP`
- Can `route information`
- Can `station information` theo he sinh thai giao thong cong cong
- Can `realtime` nhu train location, train status, GTFS-RT
- Can demo nghiep vu van hanh tau

Dung `MLIT N02/N03` khi:

- Can `danh sach ga toan quoc`
- Can `loc ga theo prefecture`
- Can du lieu nen GIS chinh thong
- Can bao phu cac khu vuc ma ODPT khong phai lua chon tot
- Can bai toan khong gian hanh chinh nhu `ga nao nam trong Tokyo/Miyazaki/Shimane`

Khuyen nghi thuc te cho project:

- `Station catalog theo prefecture`: dung `MLIT N02 + N03`
- `Railway line de ve 3D`: uu tien `ODPT GTFS/GTFS-JP`
- `Realtime trains`: uu tien `ODPT Train Location / GTFS-RT`
- `Normalized backend`: chap nhan ingest ca `ODPT` va `MLIT`, khong co ly do ky thuat de ep chi dung 1 nguon

### 1.2 Muc do dam bao du lieu theo nguon

Khong nen hieu rang bo nguon hien tai `dam bao day du 100%` cho moi muc tieu.

Danh gia thuc te:

- `MLIT N02 + N03`:
  - Muc do dam bao `cao` cho bai toan `danh sach ga theo prefecture`
  - Ly do: day la du lieu nen quoc gia do `MLIT` cong bo, pham vi toan quoc
- `ODPT GTFS/GTFS-JP`:
  - Muc do dam bao `kha cao` cho bai toan `ve tuyen len Cesium`
  - Ly do: GTFS co `shapes.txt`, `routes.txt`, `trips.txt`, `stop_times.txt`
  - Rui ro: chat luong/do day cua feed phu thuoc tung operator
- `ODPT Train Location / GTFS-RT / Train Status`:
  - Muc do dam bao `trung binh` cho bai toan `active trains + current position`
  - Ly do: du lieu realtime phu thuoc operator va khong phai feed nao cung co `real position`
  - Can thiet ke fallback `estimated` hoac `status_only`

Chung cu tu tai lieu chinh thuc:

- `MLIT N02` la du lieu `railway data` toan quoc, bao gom route va station:
  - https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
- `MLIT N03` la du lieu `administrative boundary`, dung de loc theo prefecture:
  - https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
- `GTFS Realtime VehiclePosition` co nhieu truong la `optional`; tai lieu chuan ghi ro `position`, `current_stop_sequence`, `stop_id`, `current_status`, `timestamp` deu co the la tuy chon o feed:
  - https://gtfs.org/documentation/realtime/reference/
  - https://gtfs.org/ja/documentation/realtime/reference/
- ODPT cung cap dataset theo tung operator/dataset, vi vay phai kiem tra availability theo operator chu khong the gia dinh dong deu:
  - Toei train location: https://ckan.odpt.org/dataset/r_train_location-toei
  - Yokohama train location: https://ckan.odpt.org/dataset/r_train_location-yokohamamunicipal
  - Tokyo Metro train status: https://ckan.odpt.org/en/dataset/r_train_status-tokyometro
  - Tokyo Metro GTFS-RT: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-tokyometro

Ket luan scope:

- Co the cam ket kha chac chan:
  - danh sach ga theo prefecture
  - geometry tuyen de ve 3D
- Khong nen cam ket tuyet doi:
  - moi tau deu co GPS that
  - moi operator deu co realtime feed day du

### 2. Kha nang dap ung 3 muc tieu

#### Muc tieu 1 - Lay thong tin cac tram/ga

Co kha nang lam duoc.

Du lieu phu hop:

- `Station information`
- `GTFS stops.txt`
- `MLIT N02 railway station data`
- `MLIT N03 administrative boundary data`

Chi tiet thong tin co the lay:

- `station_id` hoac `stop_id`: dinh danh ga/tram
- `station_code` / `stop_code`: ma ga public-facing neu operator cung cap
- `station_name` / `stop_name`: ten ga
- `latitude`, `longitude`: toa do de ve marker tren map
- `operator_id`: ga thuoc operator nao
- `railway_id` / `route_id`: ga thuoc tuyen nao
- `location_type`: phan biet station, platform, entrance/exit neu lay tu GTFS
- `parent_station`: lien ket platform/boarding area voi ga cha
- `wheelchair_boarding`: thong tin tiep can neu feed GTFS co
- `station_order`: thu tu ga tren tuyen, suy ra tu route/stop sequence
- `previous_station`, `next_station`: ga truoc va ga sau, suy ra tu route/stop sequence

Cach lay:

1. Lay `Station information` tu ODPT cho tung operator de co bo du lieu ga nghiep vu.
   Tai lieu/nguon:
   - ODPT developer site: https://developer.odpt.org/
   - Toei Station information dataset: https://ckan.odpt.org/dataset/r_station-toei
   - Tokyo Metro Station information dataset: https://ckan.odpt.org/en/dataset/r_station-tokyometro
   - Danh sach station-information datasets tren CKAN ODPT: https://ckan.odpt.org/dataset/?_organization_limit=0&license_id=odpt-ptodbl&tags=%E9%A7%85%E6%83%85%E5%A0%B1-station_information
2. Lay `GTFS stops.txt` de bo sung cac field theo chuan GTFS, dac biet la `stop_lat`, `stop_lon`, `location_type`, `parent_station`.
   Tai lieu/nguon:
   - GTFS reference: https://gtfs.org/resources/gtfs/
   - GTFS schedule reference `stops.txt`: https://gtfs.org/ja/documentation/schedule/reference/
   - GTFS examples `routes, stops, trips`: https://gtfs.org/ja/documentation/schedule/examples/routes-stops-trips/
3. Lay `GTFS stop_times.txt` va/hoac `Route information` de suy ra thu tu ga tren tuyen.
   Tai lieu/nguon:
   - GTFS schedule reference: https://gtfs.org/ja/documentation/schedule/reference/
   - Toei Route information dataset: https://ckan.odpt.org/dataset/r_route-toei
   - Yokohama Route information dataset: https://ckan.odpt.org/en/dataset/r_route-yokohamamunicipal
4. Normalize thanh 1 model ga chung trong backend, vi ten field va muc do day du co the khac nhau giua cac operator.
5. Neu bai toan la `lay danh sach ga theo prefecture`, dung `MLIT N02` de lay toan bo ga va dung `MLIT N03` de loc theo bien gioi hanh chinh.
   Tai lieu/nguon:
   - MLIT railway data catalog `N02`: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
   - File rail station data da dung trong tool: https://nlftp.mlit.go.jp/ksj/gml/data/N02/N02-24/N02-24_GML.zip
   - MLIT administrative boundary catalog `N03`: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
   - Vi du prefecture boundary files:
     - Tokyo: https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_13_GML.zip
     - Shimane: https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_32_GML.zip
     - Miyazaki: https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_45_GML.zip

API/catalog co the su dung trong phan "cach lay":

- ODPT developer portal:
  - Dang ky va quan ly API key: https://developer.odpt.org/
- ODPT CKAN dataset catalog:
  - Duyet dataset theo web catalog, sau do lay resource JSON/GTFS tu tung dataset page.
- CKAN Action API:
  - Tai lieu CKAN API: https://docs.ckan.org/en/2.11/api/
  - Vi `ckan.odpt.org` la mot CKAN catalog, co the khai thac metadata dataset qua cac action API chuan cua CKAN nhu:
    - `package_show`: `https://ckan.odpt.org/api/3/action/package_show?id=r_station-toei`
    - `package_search`: `https://ckan.odpt.org/api/3/action/package_search?q=station_information`
  - Luu y: 2 endpoint tren la cach dung suy ra tu tai lieu chuan CKAN va cau truc site `ckan.odpt.org`; chung rat phu hop de lay metadata/danh sach resource cua dataset, sau do moi truy cap resource JSON/GTFS thuc te.
- MLIT download catalogs:
  - Railway data `N02`: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
  - Administrative boundary `N03`: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html
  - Luu y: MLIT khong theo kieu CKAN API nhu ODPT. Cach su dung thuc te la lay zip GeoJSON/GML/Shapefile tu catalog page va xu ly offline trong tool/backend.

Cach hieu vai tro cua tung nguon:

- `Station information`:
  - Tot cho metadata nghiep vu cua ga
  - Tot cho mapping ga <-> operator <-> railway
- `GTFS stops.txt`:
  - Tot cho toa do va cau truc location theo chuan GTFS
  - Tot cho bai toan station/platform/parent_station
- `GTFS stop_times.txt`:
  - Tot cho bai toan thu tu ga, ga truoc, ga sau tren moi trip/line
- `MLIT N02 railway station data`:
  - Tot cho bai toan danh sach ga toan quoc
  - Tot cho loc ga theo prefecture
  - Phu hop khi ODPT khong bao phu tot ngoai cac operator/khu vuc lon
- `MLIT N03 administrative boundary data`:
  - Tot cho bai toan loc ga theo ranh gioi hanh chinh
  - Dung de map ga vao Tokyo, Miyazaki, Shimane va cac prefecture khac

Ket luan thuc te:

- Co the lay duoc danh sach ga, ten ga, ma ga, toa do, operator, line va thu tu ga tren tuyen.
- `Ga truoc/ga sau` thuong khong phai 1 field raw co san, ma la field can suy ra tu route sequence hoac stop_times.
- `Platform`, `entrance`, `boarding area` co the lay duoc neu GTFS cua operator co khai bao day du `location_type` va `parent_station`.
- Neu muc tieu chi la `lay danh sach ga theo prefecture`, huong `MLIT N02 + N03` la phu hop va de trien khai hon so voi ODPT.

Bang tom tat du lieu ga/tram:

| Muc du lieu | Nguon lay | Field chinh | Doc/API | Co can suy ra khong |
| --- | --- | --- | --- | --- |
| Dinh danh ga | `Station information`, `GTFS stops.txt` | `station_id`, `stop_id`, `stop_code` | ODPT datasets, GTFS `stops.txt` | Khong |
| Ten ga | `Station information`, `GTFS stops.txt` | `station_name`, `stop_name` | ODPT datasets, GTFS `stops.txt` | Khong |
| Toa do ga | `GTFS stops.txt`, mot phan `Station information` | `stop_lat`, `stop_lon` | GTFS `stops.txt` | Khong |
| Operator cua ga | `Station information` | `operator_id` | ODPT station datasets | Thuong khong |
| Tuyen ma ga thuoc ve | `Station information`, `Route information`, `GTFS routes/trips/stop_times` | `railway_id`, `route_id` | ODPT route/station datasets, GTFS refs | Doi khi can join |
| Thu tu ga tren tuyen | `Route information`, `GTFS stop_times.txt` | `stop_sequence` | GTFS schedule ref, ODPT route datasets | Co |
| Ga truoc / ga sau | `GTFS stop_times.txt`, `Route information` | derived from `stop_sequence` | GTFS schedule ref, ODPT route datasets | Co |
| Platform / ga cha | `GTFS stops.txt` | `location_type`, `parent_station` | GTFS `stops.txt` | Khong |
| Tiep can / wheelchair | `GTFS stops.txt` | `wheelchair_boarding` | GTFS `stops.txt` | Khong |
| Danh sach ga theo prefecture | `MLIT N02 railway station data` + `MLIT N03 administrative boundary data` | station geometry + prefecture polygon | MLIT N02/N03 catalogs | Co |

#### Muc tieu 2 - Lay thong tin cac tuyen duong sat de ve 3D

Co kha nang lam duoc.

Du lieu phu hop:

- `GTFS shapes.txt`
- `GTFS routes.txt`
- `Route information`

Chi tiet thong tin co the lay:

- `shape_id`: dinh danh hinh hoc cua tuyen/hanh trinh
- `shape_pt_lat`, `shape_pt_lon`: cac diem toa do de ve polyline
- `shape_pt_sequence`: thu tu diem tren polyline
- `route_id`: dinh danh tuyen
- `route_short_name`, `route_long_name`: ten ngan/ten day du cua tuyen
- `trip_id`: lien ket giua trip va shape
- `service_id`: de biet trip nao dang thuoc lich nao
- `station list of route`: danh sach ga cua tuyen
- `operator_id`: tuyen thuoc operator nao

Cach lay:

1. Lay bo `GTFS/GTFS-JP` cua operator tu dataset catalog ODPT.
   Tai lieu/nguon:
   - ODPT developer site: https://developer.odpt.org/
   - Toei Train information (GTFS/GTFS-JP): https://ckan.odpt.org/dataset/train-toei
   - Yokohama Train information (GTFS/GTFS-JP): https://ckan.odpt.org/dataset/yokohama_municipal_train
   - Tokyo Metro Train information (GTFS/GTFS-JP): https://ckan.odpt.org/dataset/train-tokyometro
2. Parse `shapes.txt` de lay geometry goc cho polyline.
   Tai lieu/nguon:
   - GTFS reference: https://gtfs.org/resources/gtfs/
   - GTFS schedule reference: https://gtfs.org/documentation/schedule/reference/
3. Parse `routes.txt`, `trips.txt`, `stop_times.txt` de mapping `shape_id -> trip_id -> route_id -> stop sequence`.
   Tai lieu/nguon:
   - GTFS schedule reference: https://gtfs.org/documentation/schedule/reference/
   - GTFS example for routes, stops, trips: https://gtfs.org/ja/documentation/schedule/examples/routes-stops-trips/
4. Lay them `Route information` tu ODPT de bo sung metadata nghiep vu cua tuyen.
   Tai lieu/nguon:
   - Toei Route information dataset: https://ckan.odpt.org/dataset/r_route-toei
   - Yokohama Route information dataset: https://ckan.odpt.org/en/dataset/r_route-yokohamamunicipal
   - Tokyo Metro Route information dataset: https://ckan.odpt.org/dataset/r_route-tokyometro
5. Normalize geometry thanh mang toa do de FE Cesium co the ve truc tiep.

API/catalog co the su dung trong phan "cach lay":

- CKAN dataset metadata:
  - `package_show`: `https://ckan.odpt.org/api/3/action/package_show?id=train-toei`
  - `package_show`: `https://ckan.odpt.org/api/3/action/package_show?id=r_route-toei`
- CKAN API docs:
  - https://docs.ckan.org/en/2.11/api/
- CesiumJS docs cho polyline:
  - https://cesium.com/learn/cesiumjs/ref-doc/Polyline.html
  - https://cesium.com/learn/cesiumjs/ref-doc/PolylineGraphics.html

Cach hieu vai tro cua tung nguon:

- `GTFS shapes.txt`:
  - Nguon geometry chinh de ve line
  - Tot nhat cho polyline 3D/2D
- `GTFS routes.txt`:
  - Metadata route
  - Ten tuyen, phan loai route
- `GTFS trips.txt`:
  - Noi route voi shape
- `GTFS stop_times.txt`:
  - Xac dinh danh sach ga theo thu tu tren tung trip
- `Route information`:
  - Bo sung quan he nghiep vu giua line va stations
  - Phu hop cho ten goi, phan nhom tuyen, operator-facing metadata

Ket luan:

- Neu muc tieu la ve duoc tuyen len map 3D thi nguon geometry nen uu tien la `GTFS shapes.txt`.
- `Route information` phu hop de bo sung metadata va quan he line/station, khong nen la geometry source chinh neu da co shapes.

Bang tom tat du lieu tuyen duong sat de ve 3D:

| Muc du lieu | Nguon lay | Field chinh | Doc/API | Co can suy ra khong |
| --- | --- | --- | --- | --- |
| Dinh danh tuyen | `GTFS routes.txt`, `Route information` | `route_id`, `railway_id` | GTFS refs, ODPT route datasets | Doi khi can join |
| Ten tuyen | `GTFS routes.txt`, `Route information` | `route_short_name`, `route_long_name` | GTFS refs, ODPT route datasets | Thuong khong |
| Geometry tuyen | `GTFS shapes.txt` | `shape_id`, `shape_pt_lat`, `shape_pt_lon`, `shape_pt_sequence` | GTFS schedule ref | Khong |
| Mapping shape -> route | `GTFS trips.txt`, `GTFS routes.txt` | `trip_id`, `shape_id`, `route_id` | GTFS schedule ref | Co |
| Danh sach ga tren tuyen | `GTFS stop_times.txt`, `Station information`, `Route information` | `stop_id`, `stop_sequence` | GTFS refs, ODPT station/route datasets | Co |
| Operator cua tuyen | `Route information`, `Station information`, mot phan `GTFS agency.txt/routes.txt` | `operator_id` | ODPT datasets, GTFS refs | Doi khi can join |
| Du lieu de ve tren Cesium | geometry normalized tu `shapes.txt` | `[lon, lat, height]` | Cesium polyline docs | Co |

#### Muc tieu 3 - Lay danh sach tau dang hoat dong va vi tri hien tai

Co kha nang lam duoc, nhung phu thuoc vao tung operator.

Du lieu phu hop:

- `Train location information`
- `GTFS-RT VehiclePosition` neu operator co
- `Train status / Alert`

Chi tiet thong tin co the lay:

- `train_id`: dinh danh tau
- `train_number`: so hieu tau
- `operator_id`: operator cua tau
- `railway_id` / `route_id`: tau dang chay tren tuyen nao
- `direction`: huong di chuyen
- `current_station`: tau dang dung o ga nao neu feed co
- `next_station`: ga tiep theo neu feed co
- `previous_station`: ga vua roi neu co the suy ra
- `from_station`, `to_station`: tau dang nam giua doan nao
- `latitude`, `longitude`: toa do that neu feed co
- `delay_seconds` hoac train status: thong tin tre/cham neu co
- `observed_at`: thoi diem cap nhat
- `position_type`: `real` hoac `estimated`
- `operation_status`: chuan hoa nghiep vu nhu `stopped_at_station`, `moving_to_next_station`, `departed_previous_station`

Cach lay:

1. Lay `Train location information` JSON cho nhung operator co dataset location rieng.
   Tai lieu/nguon:
   - Toei Train location: https://ckan.odpt.org/dataset/r_train_location-toei
   - Yokohama Train location: https://ckan.odpt.org/dataset/r_train_location-yokohamamunicipal
2. Lay them `Train status information` de bo sung nghia nghiep vu khi location feed khong du.
   Tai lieu/nguon:
   - Yokohama Train status: https://ckan.odpt.org/en/dataset/r_train_status-yokohamamunicipal
3. Neu operator co GTFS-RT thi lay them `VehiclePosition`, `TripUpdate`, `Alert`.
   Tai lieu/nguon:
   - Toei GTFS-RT realtime: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei
   - Tokyo Metro GTFS-RT realtime: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-tokyometro
   - Yokohama GTFS-RT realtime: https://ckan.odpt.org/dataset/r_train_gtfs_rt-yokohamamunicipal
   - GTFS Realtime reference: https://gtfs.org/documentation/realtime/reference/
4. Normalize tat ca ve mot model chung de khong phu thuoc schema rieng cua tung operator.
5. Neu feed khong co `lat/lng`, noi suy vi tri tu `from_station -> to_station` tren geometry cua line.

API/catalog co the su dung trong phan "cach lay":

- CKAN metadata:
  - `package_show`: `https://ckan.odpt.org/api/3/action/package_show?id=r_train_location-toei`
  - `package_show`: `https://ckan.odpt.org/api/3/action/package_show?id=r_train_gtfs_rt-odpt_train-toei`
- CKAN API docs:
  - https://docs.ckan.org/en/2.11/api/
- GTFS Realtime docs:
  - https://gtfs.org/documentation/realtime/reference/
- GTFS-RT VehiclePosition reference bo tro:
  - https://gtfs-llm-translation.odpt.org/documentation/realtime/feed-entities/vehicle-positions/

Cach hieu vai tro cua tung nguon:

- `Train location information JSON`:
  - De nhin nhat cho demo vi thuong da la JSON nghiep vu
  - Thuong co current/from/to station
- `GTFS-RT VehiclePosition`:
  - Tot nhat neu can vi tri that theo chuan realtime
  - Can parser Protocol Buffers
- `GTFS-RT TripUpdate`:
  - Tot cho stop progress, next stop, delay
- `Train status / Alert`:
  - Tot cho line status, disruption, fallback nghiep vu

Bang tom tat du lieu tau dang hoat dong va vi tri hien tai:

| Muc du lieu | Nguon lay | Field chinh | Doc/API | Co can suy ra khong |
| --- | --- | --- | --- | --- |
| Dinh danh tau | `Train location`, `GTFS-RT VehiclePosition/TripUpdate` | `train_id`, `vehicle_id`, `trip_id`, `train_number` | ODPT realtime datasets, GTFS-RT refs | Doi khi can mapping |
| Tau dang hoat dong | `Train location`, `TripUpdate` | active train records | ODPT realtime datasets, GTFS-RT refs | Thuong khong |
| Tuyen tau dang chay | `Train location`, `TripUpdate`, `GTFS trips/routes` | `railway_id`, `route_id`, `trip_id` | ODPT datasets, GTFS refs | Doi khi can join |
| Ga hien tai | `Train location`, `VehiclePosition`, `TripUpdate` | `current_station`, `current_stop_sequence` | ODPT realtime, GTFS-RT refs | Doi khi can suy ra |
| Ga tiep theo | `Train location`, `TripUpdate` | `next_station`, `stop_sequence` | ODPT realtime, GTFS-RT refs | Co the can suy ra |
| Ga vua roi | `Train location`, `TripUpdate`, `stop_times` | `previous_station` | GTFS refs, ODPT realtime | Co |
| Doan ga hien tai | `Train location` | `from_station`, `to_station` | ODPT train location datasets | Thuong khong |
| Toa do that | `VehiclePosition` hoac feed location co lat/lng | `latitude`, `longitude` | GTFS-RT refs, operator resource | Co the khong co |
| Vi tri estimated | geometry line + `from_station/to_station` | derived point on shape | GTFS shapes + train location | Co |
| Trang thai nghiep vu | location + status + trip progress | `stopped_at_station`, `moving_to_next_station`, `departed_previous_station` | normalized internal model | Co |
| Delay / disruption | `TripUpdate`, `Train status`, `Alert` | delay fields, status text | GTFS-RT refs, ODPT status datasets | Doi khi can mapping |
| Thoi diem cap nhat | realtime feed | `timestamp`, `observed_at` | GTFS-RT refs, operator resources | Thuong khong |

Ket luan:

- Khong nen cam ket moi operator deu co GPS chinh xac.
- Thuc te se co 3 muc:
  - co `real position`
  - chi co `estimated position` dua tren doan giua 2 ga
  - chi co `status/alert`, khong du de ve moving train

### 3. Khu vuc/operator nen uu tien

Uu tien theo `prefecture scope`:

- `Tokyo`
- `Miyazaki`
- `Shimane`

Ly do:

- Phu hop voi goal `station catalog theo prefecture`
- Da co the dung `MLIT N02 + N03` de lay ga va `MLIT RailroadSection` de ve line
- Giu duoc scope nho va de demo tren Cesium

Uu tien theo `operator scope` cho trip/realtime:

- `Tokyo Toei`
- `Yokohama Municipal Subway`

Ly do:

- Co du lieu static phu hop cho station + route + shape
- Co dataset train location rieng
- Rui ro scope va license de kiem soat hon cho phase nghiep vu realtime

Can can nhac them:

- `Tokyo Metro`: phu hop cho station/route/static, nhung khong phai lua chon uu tien nhat neu muc tieu chinh la tracking moving trains
- `JR East`, `Tobu`, `Keikyu`: co the huu ich, nhung can review ky phan license va do day cua realtime feed

### 4. Cac ket luan ky thuat chinh

- Nen tach `raw data` va `normalized data`
- Nen co `PostgreSQL/PostGIS` de luu geometry va ho tro query map
- Geometry de ve 3D nen normalize thanh polyline coordinates
- Realtime train data nen normalize thanh mot model chung giua cac operator
- FE chi nen an du lieu normalized tu backend, khong doc truc tiep raw feed

### 5. Rui ro chinh

- Dang ky API key ODPT co the mat toi da `2 business days`
- License moi operator khac nhau
- Khong phai operator nao cung co realtime location o muc GPS that
- Schema co the khac nhau giua cac feed/operator
- Realtime feed co the tre, trong, hoac thay doi cau truc

### 6. Ket luan cuoi

Huong kha thi nhat cho demo la:

- Dung `MLIT N02 + N03` cho `station catalog` cua `Tokyo`, `Miyazaki`, `Shimane`
- Dung `MLIT N02 RailroadSection` cho line demo tren Cesium
- Dung `ODPT GTFS/GTFS-JP` cho `trip`
- Dung `Train location / GTFS-RT` cho tracking tau dang hoat dong
- Chap nhan mo hinh `real position` va `estimated position` thay vi cam ket GPS chinh xac cho tat ca

## Tai lieu va nguon tham khao cho du lieu ga/tram

- ODPT developer site: https://developer.odpt.org/
- Toei Station information dataset: https://ckan.odpt.org/dataset/r_station-toei
- Tokyo Metro Station information dataset: https://ckan.odpt.org/en/dataset/r_station-tokyometro
- ODPT station-information dataset listing theo Public Transportation Open Data Basic License: https://ckan.odpt.org/dataset/?_organization_limit=0&license_id=odpt-ptodbl&tags=%E9%A7%85%E6%83%85%E5%A0%B1-station_information
- GTFS reference: https://gtfs.org/resources/gtfs/
- GTFS schedule reference `stops.txt`: https://gtfs.org/ja/documentation/schedule/reference/
- GTFS example for routes, stops, trips: https://gtfs.org/ja/documentation/schedule/examples/routes-stops-trips/
