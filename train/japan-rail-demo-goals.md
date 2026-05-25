# Japan Rail Demo Goals

Cap nhat: 2026-05-25

## Muc tieu

Demo se tap trung vao 3 muc tieu chinh:

1. Lay duoc danh sach cac tram/ga cua `Tokyo`, `Miyazaki`, `Shimane`.
2. Ve duoc cac tuyen duong sat va tram/ga len `Cesium`.
3. Lay duoc `trip` va thong tin `realtime` cua tau theo muc du lieu nguon cho phep.

## Dien giai tung muc tieu

### 1. Thong tin tram/ga

Can lay duoc:

- Danh sach ga
- Ten ga
- Ma ga neu co
- Toa do
- Tuyen ma ga thuoc ve
- Thu tu ga tren tuyen
- Ga truoc va ga sau
- Prefecture cua ga

Ket qua mong muon:

- Co the tra cuu va hien thi thong tin ga cua `Tokyo`, `Miyazaki`, `Shimane` tren map

### 2. Thong tin tuyen duong sat de ve 3D

Can lay duoc:

- Danh sach cac tuyen
- Geometry cua tuyen de ve polyline
- Danh sach ga thuoc tuyen
- Operator cua tuyen

Ket qua mong muon:

- Co the ve duoc cac tuyen duong sat len ban do 3D bang CesiumJS
- Co the hien thi duoc ca line va station tren cung mot map

### 3. Danh sach tau dang hoat dong va vi tri hien tai

Can lay duoc:

- `trip`
- `route`
- `stop sequence`
- Danh sach tau dang hoat dong
- Tau dang dung o ga nao
- Tau dang di den ga nao
- Tau vua roi ga nao
- Vi tri hien tai la `real` hay `estimated`
- Thoi diem cap nhat cuoi cung

Ket qua mong muon:

- Co the hien thi tau dang hoat dong tren map va bieu dien duoc trang thai van hanh hien tai

## Pham vi demo

In scope:

- Hien thi station data
- Hien thi railway lines tren map 3D
- Hien thi `trip`, active trains va trang thai hien tai

Out of scope:

- Cover all Japan
- Route planning cho end-user
- ETA/prediction phuc tap
- Cam ket GPS chinh xac cho moi operator

## Ghi chu

- `MLIT N02 + N03` dung cho station catalog theo prefecture
- `MLIT N02 RailroadSection` hien dang du de ve line tren Cesium cho demo dau
- `ODPT GTFS / GTFS-JP` la huong dung cho `trip` va geometry theo operator neu can chi tiet hon
- Realtime train location co the la `real position`, `estimated position`, hoac `status only` tuy theo operator/feed
