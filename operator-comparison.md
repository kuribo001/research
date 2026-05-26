# Operator Comparison

Cap nhat: 2026-05-26

## Ghi chu

Bang so sanh duoi day duoc tong hop tu:

- [station_export_tool/stations_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/station_export_tool/stations_tokyo_miyazaki_shimane.json:1)
- [line_export_tool/rail_lines_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/line_export_tool/rail_lines_tokyo_miyazaki_shimane.json:1)

Y nghia cac cot:

- `Stations in scope`: so ga cua operator xuat hien trong prefecture scope hien tai
- `Lines in station data`: so ten line khac nhau gan voi operator trong station JSON
- `Lines in line data`: so line dang co trong line export JSON
- `Sections in line data`: so line sections trong data line export
- `Trip/realtime priority`: goi y uu tien cho phase `trip/realtime`

Luu y:

- Day la so lieu trong `scope hien tai`, khong phai thong ke toan bo mang luoi quoc gia cua tung operator.
- `Trip/realtime priority` la de xuat thuc dung cho project, khong phai xep hang chinh thong.

Them nua:

- `Trip source de xuat` va `Realtime source de xuat` ben duoi la tong hop tu research hien tai.
- Neu ghi `chua xac minh`, nghia la chua tim duoc bang chung du ro ve feed/API machine-readable trong scope research nay.

## Tokyo

| Operator | Internal key | Stations in scope | Lines in station data | Lines in line data | Sections in line data | Trip/realtime priority | Ghi chu |
| --- | --- | ---: | ---: | ---: | ---: | --- | --- |
| 東京都 | `mlit:toei` | 140 | 36 | 6 | 317 | High | Operator da duoc chot cho phase trip/realtime trong Tokyo |

### Ket luan cho Tokyo

- Operator da duoc chot cho `trip/realtime`: `Toei`
- `Tokyo Metro` chi la phuong an mo rong sau do

### Trip/realtime comparison cho Tokyo

| Operator | Internal key | Trip source de xuat | Realtime source de xuat | Nguon / Link | Muc san sang | Ghi chu |
| --- | --- | --- | --- | --- | --- | --- |
| 東京都 | `mlit:toei` | `ODPT GTFS/GTFS-JP` | `ODPT Train Location`, `ODPT GTFS-RT` | Trip: https://ckan.odpt.org/ja/dataset/train-toei ; Train location: https://ckan.odpt.org/dataset/r_train_location-toei ; GTFS-RT: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei | Cao | Operator da duoc chot |

### Nguon chinh thuc da xac minh cho Tokyo

- `東京都` / Toei
  - GTFS/GTFS-JP:
    - https://ckan.odpt.org/ja/dataset/train-toei
  - Train location:
    - https://ckan.odpt.org/dataset/r_train_location-toei
  - GTFS-RT:
    - https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei
  - Danh gia:
    - Trip: `co`
    - Realtime: `co co so tot`
- `東京地下鉄` / Tokyo Metro
  - Khong nam trong operator scope da chot cho Tokyo o giai doan hien tai
- `東日本旅客鉄道` / JR East
  - Khong nam trong operator scope da chot cho Tokyo o giai doan hien tai

## Miyazaki

| Operator | Internal key | Stations in scope | Lines in station data | Lines in line data | Sections in line data | Trip/realtime priority | Ghi chu |
| --- | --- | ---: | ---: | ---: | ---: | --- | --- |
| 九州旅客鉄道 | `mlit:jr_kyushu` | 76 | 5 | 5 | 170 | High | Operator duy nhat trong Miyazaki scope hien tai |

### Ket luan cho Miyazaki

- Operator can chon cho phase `trip`: `JR Kyushu`
- Khong can phan van ve operator selection trong Miyazaki scope vi hien tai chi co 1 operator trong data

### Trip/realtime comparison cho Miyazaki

| Operator | Internal key | Trip source de xuat | Realtime source de xuat | Nguon / Link | Muc san sang | Ghi chu |
| --- | --- | --- | --- | --- | --- | --- |
| 九州旅客鉄道 | `mlit:jr_kyushu` | JR Kyushu timetable portal / PDF timetable; khong nen dat trong tam vao `GTFS Data Repository` | JR Kyushu Train Navi / official operation info | GTFS repo API docs: https://docs.gtfs-data.jp/api.v2.html ; API check 2026-05-26: `https://api.gtfs-data.jp/v2/files?pref=45` va `https://api.gtfs-data.jp/v2/feeds?pref=45` deu tra `[]` ; Timetable portal: https://www.jrkyushu-timetable.jp/ ; Miyazaki station timetable: https://www.jrkyushu-timetable.jp/cgi-bin/sp/sp-tt_dep.cgi/2890301/1000 ; Miyazaki area PDF: https://www.jrkyushu.co.jp/byarea/kagoshima/info/__icsFiles/afieldfile/2026/03/12/nippo.kitto.pdf ; Operation info: https://www.jrkyushu.co.jp/trains/info/ ; Train Navi/app: https://www.jrkyushu.co.jp/app/lp/ ; https://www.jrkyushu.co.jp/contact/ | Trip = trung binh, realtime = thap den trung binh | Operator duy nhat trong Miyazaki scope hien tai; can parser timetable neu muon co trip static |

### Nguon chinh thuc da xac minh cho Miyazaki

- `九州旅客鉄道` / JR Kyushu
  - GTFS Data Repository kiem tra ngay `2026-05-26`:
    - `https://api.gtfs-data.jp/v2/files?pref=45` -> `HTTP 200`, body `[]`
    - `https://api.gtfs-data.jp/v2/feeds?pref=45` -> `HTTP 200`, body `[]`
  - Official timetable sources:
    - https://www.jrkyushu-timetable.jp/
    - https://www.jrkyushu-timetable.jp/cgi-bin/sp/sp-tt_dep.cgi/2890301/1000
    - https://www.jrkyushu.co.jp/byarea/kagoshima/info/__icsFiles/afieldfile/2026/03/12/nippo.kitto.pdf
  - Official sources de bo sung station/line mapping:
    - https://www.jrkyushu.co.jp/railway/station/
    - https://www.jrkyushu.co.jp/routemap/index.jsp
    - https://www.jrkyushu.co.jp/railway/station/map.html
    - https://www.jrkyushu.co.jp/railway/station/1191904_1601.html
    - https://www.jrkyushu.co.jp/railway/station/1191906_1601.html
    - https://www.jrkyushu.co.jp/company/info/data/line_km.html
  - Official train/guide pages:
    - https://www.jrkyushu.co.jp/english/train/index.html
    - https://www.jrkyushu.co.jp/english/guide/
  - Official service info:
    - https://www.jrkyushu.co.jp/trains/info/
  - Official app/contact pages co nhac den train navigation va train position:
    - https://www.jrkyushu.co.jp/app/lp/
    - https://www.jrkyushu.co.jp/contact/
    - https://www.jrkyushu.co.jp/trainnavi/policy/japanese.pdf
  - Danh gia:
    - Trip static: `co the lam bang timetable portal/PDF`
    - GTFS public feed: `chua tim thay feed usable cho Miyazaki`
    - Realtime machine-readable feed: `chua xac minh du`

## Shimane

| Operator | Internal key | Stations in scope | Lines in station data | Lines in line data | Sections in line data | Trip/realtime priority | Ghi chu |
| --- | --- | ---: | ---: | ---: | ---: | --- | --- |
| 西日本旅客鉄道 | `mlit:jr_west` | 68 | 3 | 3 | 157 | High | Operator lon nhat trong Shimane scope hien tai |

### Ket luan cho Shimane

- Tap trung vao `JR West`
- `JR West` la operator duoc xet cho `trip/realtime` o Shimane trong scope hien tai

### Trip/realtime comparison cho Shimane

| Operator | Internal key | Trip source de xuat | Realtime source de xuat | Nguon / Link | Muc san sang | Ghi chu |
| --- | --- | --- | --- | --- | --- | --- |
| 西日本旅客鉄道 | `mlit:jr_west` | JR West official timetable / route pages | JR West running-position web service | Timetable: https://www.westjr.co.jp/global/en/timetable/ ; Train info: https://www.westjr.co.jp/global/en/travel_support/traininfo.html ; Running position: https://global.trafficinfo.westjr.co.jp/en/ ; Sanin area: https://www.train-guide.westjr.co.jp/area_sanin.html ; Sanin line: https://www.train-guide.westjr.co.jp/sanin4.html?st=0031 | Trip = trung binh den cao, realtime = trung binh den cao | Operator duoc tap trung trong Shimane scope hien tai |

### Nguon chinh thuc da xac minh cho Shimane

- `西日本旅客鉄道` / JR West
  - Official train service info:
    - https://www.westjr.co.jp/global/en/travel_support/traininfo.html
  - Official timetable:
    - https://www.westjr.co.jp/global/en/timetable/
  - Official train running position services:
    - https://global.trafficinfo.westjr.co.jp/en/
    - https://www.train-guide.westjr.co.jp/area_sanin.html
    - https://www.train-guide.westjr.co.jp/sanin4.html?st=0031
    - https://www.train-guide.westjr.co.jp/terms.html
  - Danh gia:
    - Trip: `co co so de lam`
    - Realtime: `co co so tot hon Miyazaki/Shimane local, nhung chua xac minh la public machine-readable API`

## De xuat operator cho phase trip/realtime

Thu tu uu tien de nghi:

1. `東京都` / Toei
2. `九州旅客鉄道` / JR Kyushu
3. `西日本旅客鉄道` / JR West

Neu can toi gian hoa pha dau:

1. `Toei`
2. `JR Kyushu`
3. `JR West`

## Ket luan tong hop cho muc tieu visualize train 3D

- `Tokyo / Toei`:
  - la operator da duoc chot cho Tokyo
- `Miyazaki / JR Kyushu`:
  - co co so cho trip static tu timetable chinh thuc, nhung realtime machine-readable chua du ro
- `Shimane / JR West`:
  - co co so tot hon cho realtime via official running-position services
