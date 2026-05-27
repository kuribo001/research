# Java Code Examples

Thư mục này chứa code Java ví dụ để xử lý dữ liệu railway trong project.

## Ví dụ Hiện Có

- `ToeiGtfsExample`
  - đọc `Toei-Train-GTFS/stops.txt`
  - đọc `Toei-Train-GTFS/routes.txt`
  - đọc `Toei-Train-GTFS/trips.txt`
  - đọc `Toei-Train-GTFS/stop_times.txt`
  - suy ra ga đi / ga đến của từng trip từ `stop_times.txt`

## Cách Chạy

Từ thư mục project root:

```bash
javac -d java-code-examples/out java-code-examples/src/main/java/com/example/rail/*.java
java -cp java-code-examples/out com.example.rail.ToeiGtfsExample
```

Mặc định chương trình đọc dữ liệu từ:

```text
Toei-Train-GTFS
```

Có thể truyền path khác:

```bash
java -cp java-code-examples/out com.example.rail.ToeiGtfsExample /path/to/gtfs-folder
```
