# Kết Luận Nghiên Cứu Japan Rail

Cập nhật: 2026-05-26

## 1. Kết Luận Tổng Quan

Không có một nguồn dữ liệu duy nhất đáp ứng tốt toàn bộ mục tiêu.

Chiến lược phù hợp hiện tại:

- `MLIT N02 + N03` cho station catalog theo prefecture.
- `MLIT N02 RailroadSection` cho line geometry trên Cesium.
- `ODPT` cho `Tokyo / Toei` trip và realtime.
- `JR Kyushu` timetable/source chính thức cho `Miyazaki`.
- `JR West` timetable/train-guide cho `Shimane`.

## 2. Khi Nào Dùng ODPT, Khi Nào Dùng MLIT

Dùng `ODPT` khi:

- Cần dữ liệu theo operator.
- Cần `GTFS/GTFS-JP`.
- Cần `Train Location`, `GTFS-RT`, `Train Status`.
- Cần dữ liệu vận hành tàu ở mức nghiệp vụ.

Dùng `MLIT N02/N03` khi:

- Cần danh sách ga toàn quốc.
- Cần lọc ga theo prefecture.
- Cần dữ liệu nền GIS chính thống.
- Cần coverage cho khu vực không được ODPT hỗ trợ tốt.

Kết luận thực tế:

- `Station catalog theo prefecture`: dùng `MLIT N02 + N03`.
- `Line trên Cesium`: dùng `MLIT N02 RailroadSection`.
- `Tokyo trip/realtime`: dùng `ODPT`, chốt operator `Toei`.
- `Miyazaki trip`: dùng nguồn chính thức của `JR Kyushu`, không dựa vào ODPT.
- `Shimane trip/realtime`: dùng nguồn chính thức của `JR West`, không dựa vào ODPT.

## 3. Mức Độ Đảm Bảo Theo Mục Tiêu

| Mục tiêu | Nguồn chính | Mức đảm bảo | Ghi chú |
| --- | --- | --- | --- |
| Station catalog | `MLIT N02 + N03` | Cao | Phù hợp nhất cho `Tokyo`, `Miyazaki`, `Shimane` |
| Railway lines trên Cesium | `MLIT N02 RailroadSection` | Cao | Có thể nâng cấp bằng `GTFS shapes.txt` nếu cần theo trip/operator |
| Tokyo trip | `ODPT GTFS/GTFS-JP` của `Toei` | Cao | Toei đã được chốt |
| Tokyo realtime | `ODPT Train Location / GTFS-RT` của `Toei` | Trung bình đến cao | Cần kiểm tra field thực tế khi implement |
| Miyazaki trip | `JR Kyushu timetable portal/PDF` | Trung bình | Cần parser HTML/PDF |
| Miyazaki realtime | `JR Kyushu Train Navi / operation info` | Thấp đến trung bình | Chưa xác minh public machine-readable feed |
| Shimane trip | `JR West timetable / route pages` | Trung bình đến cao | Không lấy từ ODPT |
| Shimane realtime | `JR West train-guide` | Trung bình đến cao | Có official running-position web service, chưa xác minh public API |

## 4. Station Catalog

Nguồn dùng:

- MLIT N02 railway data: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N02-2025.html
- MLIT N03 administrative boundary data: https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2026.html

Dữ liệu có thể lấy:

- Tên ga.
- Mã ga / group code nếu có.
- Tọa độ.
- Line/operator liên quan.
- Prefecture sau khi spatial join với boundary N03.

Kết luận:

- Đây là nguồn chắc nhất cho station list theo `Tokyo`, `Miyazaki`, `Shimane`.
- ODPT không phải lựa chọn tốt nhất cho bài toán station catalog theo prefecture.

## 5. Railway Lines Cho Cesium

Nguồn đang dùng:

- `MLIT N02 RailroadSection`

Lý do:

- Có geometry tuyến sẵn.
- Lọc được theo prefecture.
- Dùng được ngay cho Cesium.

Output hiện tại:

- [line_export_tool/rail_lines_tokyo_miyazaki_shimane.json](/Users/account/Desktop/works/FPT/research/line_export_tool/rail_lines_tokyo_miyazaki_shimane.json:1)
- [line_export_tool/viewer.html](/Users/account/Desktop/works/FPT/research/line_export_tool/viewer.html:1)

Ghi chú:

- Nếu cần geometry gắn với trip/operator chi tiết hơn, có thể chuyển sang `GTFS shapes.txt`.
- Với hiện tại, MLIT đủ tốt để vẽ line trên Cesium.

## 6. Tokyo / Toei

Quyết định:

- `Tokyo` chốt operator `Toei` cho phase `trip/realtime`.

Nguồn:

- Toei GTFS/GTFS-JP: https://ckan.odpt.org/ja/dataset/train-toei
- Toei Train Location: https://ckan.odpt.org/dataset/r_train_location-toei
- Toei GTFS-RT: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei

Kết luận:

- Đây là operator tốt nhất để bắt đầu pipeline nghiệp vụ.
- Có cơ sở cho cả `trip static` và `realtime`.
- Nên implement Toei trước khi mở rộng sang operator khác.

## 7. Miyazaki / JR Kyushu

Kết quả kiểm tra:

- `https://api.gtfs-data.jp/v2/files?pref=45` -> `HTTP 200`, body `[]`.
- `https://api.gtfs-data.jp/v2/feeds?pref=45` -> `HTTP 200`, body `[]`.
- Chưa tìm thấy official public GTFS zip hoặc public API cho `JR Kyushu` trip.

Nguồn khả dụng cho `trip static`:

- JR Kyushu timetable portal: https://www.jrkyushu-timetable.jp/
- Miyazaki station timetable: https://www.jrkyushu-timetable.jp/cgi-bin/sp/sp-tt_dep.cgi/2890301/1000
- Miyazaki area PDF: https://www.jrkyushu.co.jp/byarea/kagoshima/info/__icsFiles/afieldfile/2026/03/12/nippo.kitto.pdf

Nguồn bổ sung khi normalize:

- Station directory: https://www.jrkyushu.co.jp/railway/station/
- Route map: https://www.jrkyushu.co.jp/routemap/index.jsp
- Station map browser: https://www.jrkyushu.co.jp/railway/station/map.html
- Line inventory: https://www.jrkyushu.co.jp/company/info/data/line_km.html

Nguồn tham chiếu realtime:

- Operation info: https://www.jrkyushu.co.jp/trains/info/
- App / Train Navi: https://www.jrkyushu.co.jp/app/lp/
- Train Navi policy: https://www.jrkyushu.co.jp/trainnavi/policy/japanese.pdf

Kết luận:

- `Miyazaki / JR Kyushu` có thể làm `trip static` bằng parser timetable.
- Chưa nên cam kết realtime machine-readable.
- Không nên ghi `GTFS Data Repository` là nguồn trip usable cho Miyazaki ở thời điểm này.

## 8. Shimane / JR West

Kết quả kiểm tra ODPT ngày `2026-05-26`:

- Search `jrwest`: không tìm thấy dataset.
- Search `西日本旅客鉄道`: không tìm thấy dataset.
- ODPT organization listing có `JR East`, không thấy `JR West`.

Kết luận về ODPT:

- `JR West` hiện không có `trip static` từ ODPT trong research này.
- `Shimane / JR West` phải đi theo official timetable / train-guide sources.

Nguồn dùng cho trip:

- JR West timetable: https://www.westjr.co.jp/global/en/timetable/
- JR West train service info: https://www.westjr.co.jp/global/en/travel_support/traininfo.html

Nguồn dùng cho realtime reference:

- JR West running position: https://global.trafficinfo.westjr.co.jp/en/
- Sanin area: https://www.train-guide.westjr.co.jp/area_sanin.html
- Sanin line: https://www.train-guide.westjr.co.jp/sanin4.html?st=0031
- Terms: https://www.train-guide.westjr.co.jp/terms.html

Kết luận:

- `JR West` là operator chính cho Shimane trong scope hiện tại.
- Có official running-position web service, phù hợp để nghiên cứu visualization moving trains.
- Chưa xác minh public machine-readable API; cần kiểm tra tiếp bundle JS / endpoint nội bộ nếu muốn crawler.

## 9. Rủi Ro Chính

- Không phải operator nào cũng có public GTFS hoặc ODPT feed.
- Realtime web/app service không đồng nghĩa có public API hợp lệ để crawl.
- License và terms cần kiểm tra kỹ trước khi tự động lấy dữ liệu.
- Vị trí tàu có thể là `real`, `estimated`, hoặc chỉ `status only`.
- Backend cần normalize dữ liệu từ nhiều nguồn không đồng nhất.

## 10. Kết Luận Cuối

Hướng khả thi nhất:

- `MLIT N02 + N03` cho station catalog.
- `MLIT N02 RailroadSection` cho line trên Cesium.
- `Tokyo / Toei` cho `trip/realtime` qua ODPT.
- `Miyazaki / JR Kyushu` cho `trip static` qua timetable parser.
- `Shimane / JR West` cho `trip static` qua timetable và realtime reference qua train-guide.

Không nên cam kết GPS thật hoặc realtime đầy đủ cho mọi prefecture/operator.
