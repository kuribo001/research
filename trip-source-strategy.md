# Chiến Lược Nguồn Trip

Cập nhật: 2026-05-26

## Mục Tiêu

Chốt nguồn dữ liệu `trip` và `realtime` theo `prefecture scope`:

- `Tokyo`
- `Miyazaki`
- `Shimane`

## Nguyên Tắc

- View của app là `prefecture-based`.
- Nguồn gốc của `trip/realtime` có thể là `operator-based`.
- Backend phải normalize và map về `prefecture scope`.
- Không được giả định `operator_id` khớp tự nhiên giữa nhiều nguồn; cần `internal_operator_key` và `source_mappings`.

## Tokyo

- `Trip static`: chốt `Toei`, dùng `ODPT GTFS/GTFS-JP`.
- `Realtime`: dùng `ODPT Train Location`, `GTFS-RT`.
- `Độ sẵn sàng`: cao nhất trong 3 prefecture.

Links:

- Toei GTFS: https://ckan.odpt.org/ja/dataset/train-toei
- Toei train location: https://ckan.odpt.org/dataset/r_train_location-toei
- Toei GTFS-RT: https://ckan.odpt.org/dataset/r_train_gtfs_rt-odpt_train-toei

## Miyazaki

- `Trip static`: không đặt trọng tâm vào `GTFS Data Repository`; ưu tiên `JR Kyushu timetable portal/PDF`.
- `Realtime`: mới xác minh được `JR Kyushu Train Navi` và operation info ở mức web/app service.
- `Độ sẵn sàng`: trip trung bình, realtime thấp đến trung bình.

Kiểm tra ngày `2026-05-26`:

- `https://api.gtfs-data.jp/v2/files?pref=45` -> `HTTP 200`, body `[]`.
- `https://api.gtfs-data.jp/v2/feeds?pref=45` -> `HTTP 200`, body `[]`.

Links lấy `trip static`:

- Portal: https://www.jrkyushu-timetable.jp/
- Miyazaki station timetable: https://www.jrkyushu-timetable.jp/cgi-bin/sp/sp-tt_dep.cgi/2890301/1000
- Miyazaki Jingu timetable: https://www.jrkyushu-timetable.jp/cgi-bin/sp/sp-tt_dep.cgi/2890501/
- Miyazaki area PDF: https://www.jrkyushu.co.jp/byarea/kagoshima/info/__icsFiles/afieldfile/2026/03/12/nippo.kitto.pdf

Links bổ sung khi normalize `trip`:

- JR Kyushu station directory: https://www.jrkyushu.co.jp/railway/station/
- JR Kyushu route map: https://www.jrkyushu.co.jp/routemap/index.jsp
- JR Kyushu station map browser: https://www.jrkyushu.co.jp/railway/station/map.html
- Miyazaki station page: https://www.jrkyushu.co.jp/railway/station/1191904_1601.html
- Miyazaki Jingu station page: https://www.jrkyushu.co.jp/railway/station/1191906_1601.html
- Railway line inventory: https://www.jrkyushu.co.jp/company/info/data/line_km.html

Links tham chiếu `realtime`:

- Operation info: https://www.jrkyushu.co.jp/trains/info/
- App / Train Navi: https://www.jrkyushu.co.jp/app/lp/
- Contact / train position note: https://www.jrkyushu.co.jp/contact/
- Train Navi policy: https://www.jrkyushu.co.jp/trainnavi/policy/japanese.pdf

Kết luận:

- Chưa tìm thấy `official public GTFS zip` hoặc `public API` của `JR Kyushu` cho `trip`.
- Nguồn chính thức khả dụng nhất cho `trip static` là timetable portal, station-specific timetable pages, timetable PDF, station directory và route map.
- Realtime có service cho người dùng, nhưng chưa xác nhận được machine-readable feed công khai.

## Shimane

- `Trip static`: ưu tiên `JR West` official timetable / route pages.
- Không dùng `ODPT` cho `JR West` vì không tìm thấy dataset `jrwest` / `西日本旅客鉄道` trên ODPT.
- `Realtime`: `JR West` có official running-position web service đáng chú ý.
- `Độ sẵn sàng`: trung bình.

Links:

- JR West train info: https://www.westjr.co.jp/global/en/travel_support/traininfo.html
- JR West timetable: https://www.westjr.co.jp/global/en/timetable/
- JR West running position: https://global.trafficinfo.westjr.co.jp/en/
- JR West Sanin area: https://www.train-guide.westjr.co.jp/area_sanin.html
- JR West Sanin line page: https://www.train-guide.westjr.co.jp/sanin4.html?st=0031
- ODPT search `jrwest`: https://ckan.odpt.org/dataset/?q=jrwest
- ODPT search `西日本旅客鉄道`: https://ckan.odpt.org/dataset/?q=%E8%A5%BF%E6%97%A5%E6%9C%AC%E6%97%85%E5%AE%A2%E9%89%84%E9%81%93

Kết quả kiểm tra ODPT ngày `2026-05-26`:

- `jrwest` -> không tìm thấy dataset.
- `西日本旅客鉄道` -> không tìm thấy dataset.
- ODPT organization listing có `JR East`, không thấy `JR West`.

Kết luận:

- `JR West` hiện không có `trip static` từ ODPT trong research này.
- `Shimane / JR West` phải đi theo official timetable / train-guide sources.

## Kết Luận

- `Tokyo` là prefecture tốt nhất để bắt đầu pipeline `trip/realtime`.
- `Toei` là operator đã được chốt cho `Tokyo`.
- `Miyazaki` thực tế nhất là parser `JR Kyushu timetable` thành JSON normalized.
- `Shimane` hiện tập trung vào `JR West`.
