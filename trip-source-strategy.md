# Trip Source Strategy

Cap nhat: 2026-05-26

## Muc tieu

Chot nguon du lieu `trip` va `realtime` theo `prefecture scope`:

- `Tokyo`
- `Miyazaki`
- `Shimane`

## Nguyen tac

- View cua app la `prefecture-based`
- Nguon goc cua `trip/realtime` co the la `operator-based`
- Backend phai normalize va map ve `prefecture scope`
- `operator_id` khong duoc gia dinh khop xuyen nguon; can `internal_operator_key` va `source_mappings`

## Chien luoc theo prefecture

### Tokyo

- `Trip static`
  - chot `Toei`
  - dung `ODPT GTFS/GTFS-JP`
- `Realtime`
  - dung `ODPT Train Location`, `GTFS-RT`
- `Do san sang`
  - cao nhat trong 3 prefecture

Links:

- Toei GTFS: https://ckan.odpt.org/ja/dataset/train-toei
- Toei train location: https://ckan.odpt.org/dataset/r_train_location-toei
- Toei GTFS-RT: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei

### Miyazaki

- `Trip static`
  - khong nen dat trong tam vao `GTFS Data Repository`
  - uu tien `JR Kyushu timetable portal/PDF`
- `Realtime`
  - moi xac minh duoc `JR Kyushu Train Navi` va operation info o muc web/app service
  - chua xac minh public machine-readable feed
- `Do san sang`
  - trip = trung binh
  - realtime = thap den trung binh

Kiem tra ngay `2026-05-26`:

- `https://api.gtfs-data.jp/v2/files?pref=45` -> `HTTP 200`, body `[]`
- `https://api.gtfs-data.jp/v2/feeds?pref=45` -> `HTTP 200`, body `[]`

Links lay `trip static`:

- Portal: https://www.jrkyushu-timetable.jp/
- Miyazaki station timetable: https://www.jrkyushu-timetable.jp/cgi-bin/sp/sp-tt_dep.cgi/2890301/1000
- Miyazaki Jingu timetable: https://www.jrkyushu-timetable.jp/cgi-bin/sp/sp-tt_dep.cgi/2890501/
- Miyazaki area PDF: https://www.jrkyushu.co.jp/byarea/kagoshima/info/__icsFiles/afieldfile/2026/03/12/nippo.kitto.pdf

Links bo sung de dung khi normalize `trip`:

- JR Kyushu station directory:
  - https://www.jrkyushu.co.jp/railway/station/
- JR Kyushu route map:
  - https://www.jrkyushu.co.jp/routemap/index.jsp
- JR Kyushu station map browser:
  - https://www.jrkyushu.co.jp/railway/station/map.html
- Miyazaki station page:
  - https://www.jrkyushu.co.jp/railway/station/1191904_1601.html
- Miyazaki Jingu station page:
  - https://www.jrkyushu.co.jp/railway/station/1191906_1601.html
- Railway line inventory:
  - https://www.jrkyushu.co.jp/company/info/data/line_km.html

Links tham chieu `realtime`:

- Operation info: https://www.jrkyushu.co.jp/trains/info/
- App / Train Navi: https://www.jrkyushu.co.jp/app/lp/
- Contact / train position note: https://www.jrkyushu.co.jp/contact/
- Train Navi policy: https://www.jrkyushu.co.jp/trainnavi/policy/japanese.pdf

Ket qua tim kiem mo rong:

- Chua tim thay `official public GTFS zip` hoac `public API` cua `JR Kyushu` cho `trip`
- `GTFS Data Repository` da kiem tra va khong tra feed usable cho `pref=45`
- Nguon chinh thuc kha dung nhat hien tai cho `trip static` la:
  - timetable portal
  - station-specific timetable pages
  - timetable PDF theo khu vuc
  - station directory + route map de bo sung line/station mapping
- Nguon kha dung nhat hien tai cho `realtime` la:
  - `JR Kyushu Train Navi`
  - `JR Kyushu operation info`
- Hai nguon realtime tren xac nhan co service cho nguoi dung, nhung chua xac nhan duoc machine-readable feed cong khai

### Shimane

- `Trip static`
  - uu tien `JR West` official timetable / route pages
- `Realtime`
  - `JR West` co official running-position web service dang chu y
- `Do san sang`
  - trung binh

Links:

- JR West train info: https://www.westjr.co.jp/global/en/travel_support/traininfo.html
- JR West timetable: https://www.westjr.co.jp/global/en/timetable/
- JR West running position: https://global.trafficinfo.westjr.co.jp/en/
- JR West Sanin area: https://www.train-guide.westjr.co.jp/area_sanin.html
- JR West Sanin line page: https://www.train-guide.westjr.co.jp/sanin4.html?st=0031

## Ket luan

- `Tokyo` la prefecture tot nhat de bat dau pipeline `trip/realtime`
- `Toei` la operator da duoc chot cho `Tokyo`
- `Miyazaki` hien thuc te nhat la parser `JR Kyushu timetable` thanh JSON normalized
- `Shimane` hien tap trung vao `JR West`
