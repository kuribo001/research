# Java Rail CZML Export

`TokyoRailCzmlExportService` mirrors the current Python exporter in `line_export_tool/export_japan_rail_lines.py`.

What it does:
- downloads MLIT `N02` rail sections and Tokyo `N03` boundary data
- filters to Tokyo only
- optionally filters operators using English aliases such as `Toei` or `Tokyo Metro`
- exports lightweight `CZML` with one polyline packet per rail section
- assigns one stable color per line

Main class:
- `com.example.rail.TokyoRailCzmlExportService`

Build:

```bash
cd java-code-examples
mvn -q -DskipTests compile
```

Run from CLI:

```bash
cd java-code-examples
mvn -q exec:java -Dexec.mainClass=com.example.rail.TokyoRailCzmlExportService
```

Run with custom output and operators:

```bash
cd java-code-examples
mvn -q exec:java \
  -Dexec.mainClass=com.example.rail.TokyoRailCzmlExportService \
  -Dexec.args="../line_export_tool/rail_lines_tokyo_java.czml 0.0 Toei"
```

Use from code:

```java
TokyoRailCzmlExportService service = new TokyoRailCzmlExportService();
service.exportCzml(Path.of("../line_export_tool/rail_lines_tokyo_java.czml"), 0.0, List.of("Toei"));
```
