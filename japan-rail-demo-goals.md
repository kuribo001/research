# Japan Rail Demo Goals

Cap nhat: 2026-05-26

## Muc tieu

1. Lay danh sach tram/ga cua `Tokyo`, `Miyazaki`, `Shimane`.
2. Ve duoc cac tuyen duong sat va tram/ga len `Cesium`.
3. Lay duoc `trip` va thong tin `realtime` cua tau o muc du lieu nguon cho phep.

## Ket qua can dat

- `Station`
  - co danh sach ga, ten ga, toa do, prefecture, line/operator co lien quan
- `Line`
  - co geometry de ve polyline tren Cesium
  - co the hien thi line va station tren cung mot map
- `Trip va realtime`
  - co `trip`, `route`, `stop sequence`
  - co active trains
  - co trang thai `real position`, `estimated position`, hoac `status only`

## In scope

- Station catalog theo `Tokyo`, `Miyazaki`, `Shimane`
- Railway lines va stations tren map 3D
- Trip va realtime trong pham vi nguon du lieu kha dung

## Out of scope

- Cover all Japan
- Route planning cho end-user
- ETA/prediction phuc tap
- Cam ket GPS that cho moi operator

## Ghi chu

- `MLIT N02 + N03` cho station catalog theo prefecture
- `MLIT N02 RailroadSection` dang dung de ve line demo
- `ODPT` la huong uu tien cho `trip/realtime` o `Tokyo`
- `Toei` la operator da duoc chot cho `Tokyo`
- `Miyazaki` va mot phan `Shimane` co the can timetable/local source thay vi GTFS/GTFS-RT cong khai
