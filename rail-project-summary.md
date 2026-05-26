# Tổng Hợp Project

Cập nhật: 2026-05-26

## Tổng Quan

Scope hiện tại gồm 3 prefecture: `Tokyo`, `Miyazaki`, `Shimane`.

Mục tiêu:

1. Lấy station catalog.
2. Vẽ railway lines và stations lên `Cesium`.
3. Lấy `trip` và `realtime train position` nếu nguồn dữ liệu cho phép.

## Quyết Định Dữ Liệu

- `Station catalog`: dùng `MLIT N02 + N03`.
- `Line để vẽ Cesium`: đang dùng `MLIT N02 RailroadSection`; có thể nâng cấp sang `ODPT GTFS/GTFS-JP` nếu cần geometry theo operator/trip.
- `Tokyo`: chốt `Toei`, dùng `ODPT GTFS/GTFS-JP`, `Train Location`, `GTFS-RT`.
- `Miyazaki`: `GTFS Data Repository` hiện không usable cho `pref=45`; ưu tiên `JR Kyushu timetable portal/PDF`.
- `Shimane`: tập trung `JR West`; trip static đi theo JR West timetable/route pages, không phải ODPT.

## Mức Độ Đảm Bảo

- `Station`: cao.
- `Line trên Cesium`: cao cho scope hiện tại.
- `Trip`: trung bình đến cao, phụ thuộc operator/source.
- `Realtime`: trung bình, không nên cam kết mọi operator đều có `real position`.

## Trạng Thái Hiện Tại

- Đã có station export JSON: [station_export_tool/stations_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/station_export_tool/stations_tokyo_miyazaki_shimane.json:1)
- Đã có line export JSON: [line_export_tool/rail_lines_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/line_export_tool/rail_lines_tokyo_miyazaki_shimane.json:1)
- Đã có Cesium viewer: [line_export_tool/viewer.html](/Users/account/Desktop/works/FPT/research/line_export_tool/viewer.html:1)
- Chưa có pipeline `trip/realtime` đã implement.
- Operator đầu tiên cho `Tokyo trip/realtime` là `Toei`.

## Ghi Chú Quan Trọng

- Kiểm tra ngày `2026-05-26`: `https://api.gtfs-data.jp/v2/files?pref=45` và `https://api.gtfs-data.jp/v2/feeds?pref=45` đều trả `[]`.
- `JR Kyushu / Miyazaki` chưa có GTFS public feed usable đã xác minh được.
- ODPT search ngày `2026-05-26` không tìm thấy `JR West` dataset cho `jrwest` hoặc `西日本旅客鉄道`.
- Bộ nguồn chính thức khả dụng nhất cho `JR Kyushu / Miyazaki`: timetable portal, station directory, route map, operation info, app / Train Navi.

## So Sánh Operator

Số liệu dưới đây lấy từ:

- [station_export_tool/stations_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/station_export_tool/stations_tokyo_miyazaki_shimane.json:1)
- [line_export_tool/rail_lines_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/line_export_tool/rail_lines_tokyo_miyazaki_shimane.json:1)

Các số liệu là trong `scope hiện tại`, không phải thống kê toàn bộ mạng lưới quốc gia.

### Tokyo

| Operator | Internal key | Stations in scope | Lines in station data | Lines in line data | Sections in line data | Ưu tiên trip/realtime | Ghi chú |
| --- | --- | ---: | ---: | ---: | ---: | --- | --- |
| 東京都 | `mlit:toei` | 140 | 36 | 6 | 317 | High | Operator đã được chốt cho phase trip/realtime ở Tokyo |

Kết luận:

- Operator đã được chốt cho `trip/realtime`: `Toei`.
- `Tokyo Metro` chỉ là phương án mở rộng sau đó.

Trip/realtime:

| Operator | Internal key | Trip source đề xuất | Realtime source đề xuất | Nguồn / Link | Mức sẵn sàng | Ghi chú |
| --- | --- | --- | --- | --- | --- | --- |
| 東京都 | `mlit:toei` | `ODPT GTFS/GTFS-JP` | `ODPT Train Location`, `ODPT GTFS-RT` | Trip: https://ckan.odpt.org/ja/dataset/train-toei ; Train location: https://ckan.odpt.org/dataset/r_train_location-toei ; GTFS-RT: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei | Cao | Operator đã được chốt |

Nguồn chính thức đã xác minh:

- GTFS/GTFS-JP: https://ckan.odpt.org/ja/dataset/train-toei
- Train location: https://ckan.odpt.org/dataset/r_train_location-toei
- GTFS-RT: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei

### Miyazaki

| Operator | Internal key | Stations in scope | Lines in station data | Lines in line data | Sections in line data | Ưu tiên trip/realtime | Ghi chú |
| --- | --- | ---: | ---: | ---: | ---: | --- | --- |
| 九州旅客鉄道 | `mlit:jr_kyushu` | 76 | 5 | 5 | 170 | High | Operator duy nhất trong Miyazaki scope hiện tại |

Kết luận:

- Operator cần chọn cho phase `trip`: `JR Kyushu`.
- Không cần phân vân về operator selection vì hiện tại chỉ có 1 operator trong data.

Trip/realtime:

| Operator | Internal key | Trip source đề xuất | Realtime source đề xuất | Nguồn / Link | Mức sẵn sàng | Ghi chú |
| --- | --- | --- | --- | --- | --- | --- |
| 九州旅客鉄道 | `mlit:jr_kyushu` | JR Kyushu timetable portal / PDF timetable | JR Kyushu Train Navi / official operation info | GTFS repo check: `files?pref=45` và `feeds?pref=45` đều trả `[]`; timetable portal: https://www.jrkyushu-timetable.jp/ ; Miyazaki timetable: https://www.jrkyushu-timetable.jp/cgi-bin/sp/sp-tt_dep.cgi/2890301/1000 ; PDF: https://www.jrkyushu.co.jp/byarea/kagoshima/info/__icsFiles/afieldfile/2026/03/12/nippo.kitto.pdf ; Operation info: https://www.jrkyushu.co.jp/trains/info/ ; Train Navi: https://www.jrkyushu.co.jp/app/lp/ | Trip = trung bình, realtime = thấp đến trung bình | Cần parser timetable nếu muốn có trip static |

Nguồn chính thức đã xác minh:

- GTFS Data Repository kiểm tra ngày `2026-05-26`: `pref=45` trả rỗng.
- Timetable portal: https://www.jrkyushu-timetable.jp/
- Miyazaki station timetable: https://www.jrkyushu-timetable.jp/cgi-bin/sp/sp-tt_dep.cgi/2890301/1000
- Miyazaki area PDF: https://www.jrkyushu.co.jp/byarea/kagoshima/info/__icsFiles/afieldfile/2026/03/12/nippo.kitto.pdf
- Station directory: https://www.jrkyushu.co.jp/railway/station/
- Route map: https://www.jrkyushu.co.jp/routemap/index.jsp
- Operation info: https://www.jrkyushu.co.jp/trains/info/
- Train Navi: https://www.jrkyushu.co.jp/app/lp/

### Shimane

| Operator | Internal key | Stations in scope | Lines in station data | Lines in line data | Sections in line data | Ưu tiên trip/realtime | Ghi chú |
| --- | --- | ---: | ---: | ---: | ---: | --- | --- |
| 西日本旅客鉄道 | `mlit:jr_west` | 68 | 3 | 3 | 157 | High | Operator được tập trung trong Shimane scope hiện tại |

Kết luận:

- Tập trung vào `JR West`.
- `JR West` là operator được xét cho `trip/realtime` ở Shimane trong scope hiện tại.

Trip/realtime:

| Operator | Internal key | Trip source đề xuất | Realtime source đề xuất | Nguồn / Link | Mức sẵn sàng | Ghi chú |
| --- | --- | --- | --- | --- | --- | --- |
| 西日本旅客鉄道 | `mlit:jr_west` | JR West official timetable / route pages; không có ODPT trip dataset đã xác minh | JR West running-position web service | Timetable: https://www.westjr.co.jp/global/en/timetable/ ; Train info: https://www.westjr.co.jp/global/en/travel_support/traininfo.html ; Running position: https://global.trafficinfo.westjr.co.jp/en/ ; Sanin area: https://www.train-guide.westjr.co.jp/area_sanin.html ; Sanin line: https://www.train-guide.westjr.co.jp/sanin4.html?st=0031 ; ODPT jrwest search: https://ckan.odpt.org/dataset/?q=jrwest ; ODPT Japanese search: https://ckan.odpt.org/dataset/?q=%E8%A5%BF%E6%97%A5%E6%9C%AC%E6%97%85%E5%AE%A2%E9%89%84%E9%81%93 | Trip = trung bình đến cao, realtime = trung bình đến cao | ODPT search ngày 2026-05-26 không tìm thấy `jrwest` / `西日本旅客鉄道` dataset |

Nguồn chính thức đã xác minh:

- JR West train service info: https://www.westjr.co.jp/global/en/travel_support/traininfo.html
- JR West timetable: https://www.westjr.co.jp/global/en/timetable/
- JR West running position: https://global.trafficinfo.westjr.co.jp/en/
- Sanin area: https://www.train-guide.westjr.co.jp/area_sanin.html
- Sanin line: https://www.train-guide.westjr.co.jp/sanin4.html?st=0031
- Terms: https://www.train-guide.westjr.co.jp/terms.html
- ODPT search checked on `2026-05-26`: không tìm thấy `jrwest` / `西日本旅客鉄道`.

## Thứ Tự Ưu Tiên Operator

1. `東京都` / Toei
2. `九州旅客鉄道` / JR Kyushu
3. `西日本旅客鉄道` / JR West
