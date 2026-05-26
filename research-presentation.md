# Research Presentation

Cap nhat: 2026-05-26

## 1. Bai toan

Can demo railway cho `Tokyo`, `Miyazaki`, `Shimane` voi 3 dau ra:

1. Station catalog
2. Railway lines tren `Cesium`
3. `Trip` va `realtime train position`

## 2. Ket luan tong quan

Khong co 1 nguon duy nhat phu hop cho tat ca.

Huong dung:

- `MLIT` cho station catalog va line nen
- `ODPT` cho trip/realtime o `Tokyo`
- Nguon local/operator cho `Miyazaki` va mot phan `Shimane` khi GTFS cong khai khong san

## 3. Quyet dinh nguon

- `Station`
  - `MLIT N02 + N03`
- `Line`
  - `MLIT N02 RailroadSection`
- `Trip/realtime`
  - `Tokyo`: `Toei` qua `ODPT`
  - `Miyazaki`: `JR Kyushu timetable portal/PDF` + `Train Navi`
  - `Shimane`: `JR West` timetable/pages + running-position web service

## 4. Diem can nho theo prefecture

### Tokyo

- kha nang thanh cong cao nhat cho `trip/realtime`
- da chot `Toei` lam operator chinh

### Miyazaki

- `GTFS Data Repository` da kiem tra va tra rong cho `pref=45`
- `trip static` nen lay tu `JR Kyushu timetable portal/PDF`
- Khi normalize `trip`, bo sung them:
  - station directory: https://www.jrkyushu.co.jp/railway/station/
  - route map: https://www.jrkyushu.co.jp/routemap/index.jsp
  - station map browser: https://www.jrkyushu.co.jp/railway/station/map.html
- `realtime` chua xac minh duoc machine-readable feed cong khai

### Shimane

- tap trung vao `JR West`
- co co so tot hon cho phan realtime
- ODPT search ngay `2026-05-26` khong tim thay dataset `jrwest` / `西日本旅客鉄道`
- `trip static` phai di theo JR West timetable / route pages, khong phai ODPT

## 5. Muc do dam bao

- `Station`: cao
- `Line`: cao
- `Trip`: trung binh den cao tuy prefecture/operator
- `Realtime`: trung binh, khong dong deu giua 3 prefecture

## 6. Buoc tiep theo

1. Bat dau `trip/realtime` o `Tokyo`
2. Neu can `Miyazaki`, viet parser cho `JR Kyushu timetable`
3. Neu can `Shimane`, di sau vao `JR West`
