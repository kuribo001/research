# Bus - ODPT

## Phạm Vi Cần Kiểm Tra

- Tuyến đường.
- Trạm.
- Realtime vị trí + status, ví dụ nhanh/chậm/trễ.

## Nhận Định Hiện Tại

- Hiện tại chưa thấy dữ liệu realtime position đầy đủ cho bus.
- Một số dữ liệu chỉ cho biết xe bus đã tới trạm nào hoặc chuẩn bị đi tới trạm nào.
- Nếu cần hiển thị vị trí trên map, có thể phải tự nội suy vị trí dựa trên trạm hiện tại, trạm tiếp theo và route geometry.

## Kết Luận Tạm Thời

- ODPT không cung cấp đủ dữ liệu đồng đều cho cả bus và train.
- Với bus, cần điều tra thêm theo từng operator/dataset trước khi cam kết realtime position.
