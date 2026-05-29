# Java Rail CZML Export

`TokyoRailCzmlExportService` mirrors the current Python exporter in `line_export_tool/export_japan_rail_lines.py`.

What it does:
- downloads MLIT `N02` rail sections and `N03` boundary data for Tokyo + Kanagawa
- filters to Tokyo and Kanagawa together
- optionally filters operators using English aliases such as `Toei` or `Tokyo Metro`
- exports lightweight `CZML` with one polyline packet per rail section
- can export one combined CZML or one CZML file per line
- assigns one stable color per line

Classes:
- `com.example.rail.TokyoRailCzmlExportMain`: CLI entry point
- `com.example.rail.TokyoRailCzmlExportService`: export service logic

Build:

```bash
cd java-code-examples
mvn -q -DskipTests compile
```

Run from CLI:

```bash
cd java-code-examples
mvn -q exec:java -Dexec.mainClass=com.example.rail.TokyoRailCzmlExportMain
```

Default CLI behavior:
- exports one directory of files, with one `.czml` per line
- default output directory is `../line_export_tool/rail_lines_tokyo_kanagawa_java_lines`

Run with custom output directory and operators:

```bash
cd java-code-examples
mvn -q exec:java \
  -Dexec.mainClass=com.example.rail.TokyoRailCzmlExportMain \
  -Dexec.args="../line_export_tool/rail_lines_tokyo_kanagawa_java_lines 0.0 Toei"
```

Use from code:

```java
TokyoRailCzmlExportService service = new TokyoRailCzmlExportService();
service.exportCzml(Path.of("../line_export_tool/rail_lines_tokyo_kanagawa_java.czml"), 0.0, List.of("Toei"));
service.exportCzmlPerLine(Path.of("../line_export_tool/rail_lines_tokyo_kanagawa_java_lines"), 0.0, List.of("Toei"));
```
