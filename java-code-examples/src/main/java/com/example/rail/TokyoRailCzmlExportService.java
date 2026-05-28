package com.example.rail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Exports Tokyo rail sections to lightweight CZML.
 *
 * <p>This mirrors the current Python exporter:
 * - Tokyo boundary only
 * - optional operator filtering using English aliases such as "Toei"
 * - one CZML polyline packet per rail section
 * - stable color per line
 */
public class TokyoRailCzmlExportService {
    private static final String RAILROAD_DATASET_URL =
            "https://nlftp.mlit.go.jp/ksj/gml/data/N02/N02-24/N02-24_GML.zip";
    private static final String RAILROAD_GEOJSON_PATH = "UTF-8/N02-24_RailroadSection.geojson";

    private static final PrefectureDataset TOKYO = new PrefectureDataset(
            "tokyo",
            "Tokyo",
            "東京都",
            "13",
            "https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_13_GML.zip",
            "N03-20260101_13.geojson"
    );

    private static final List<String> DEFAULT_OPERATOR_FILTERS = List.of("Toei");

    private static final Map<String, String> OPERATOR_NAME_EN = Map.ofEntries(
            Map.entry("ゆりかもめ", "Yurikamome"),
            Map.entry("京成電鉄", "Keisei"),
            Map.entry("京浜急行電鉄", "Keikyu"),
            Map.entry("京王電鉄", "Keio"),
            Map.entry("北総鉄道", "Hokuso Railway"),
            Map.entry("埼玉高速鉄道", "Saitama Railway"),
            Map.entry("多摩都市モノレール", "Tama Monorail"),
            Map.entry("小田急電鉄", "Odakyu"),
            Map.entry("御岳登山鉄道", "Mitake Tozan Railway"),
            Map.entry("東京モノレール", "Tokyo Monorail"),
            Map.entry("東京地下鉄", "Tokyo Metro"),
            Map.entry("東京臨海高速鉄道", "Tokyo Waterfront Area Rapid Transit"),
            Map.entry("東京都", "Toei"),
            Map.entry("東急電鉄", "Tokyu"),
            Map.entry("東日本旅客鉄道", "JR East"),
            Map.entry("東武鉄道", "Tobu"),
            Map.entry("東海旅客鉄道", "JR Central"),
            Map.entry("西武鉄道", "Seibu"),
            Map.entry("首都圏新都市鉄道", "Tsukuba Express"),
            Map.entry("高尾登山電鉄", "Takao Tozan Railway")
    );

    private static final Map<String, String> OPERATOR_FILTER_ALIASES = Map.ofEntries(
            Map.entry("toei", "東京都"),
            Map.entry("tokyo metropolitan bureau of transportation", "東京都"),
            Map.entry("tokyo metro", "東京地下鉄"),
            Map.entry("jr east", "東日本旅客鉄道"),
            Map.entry("jr central", "東海旅客鉄道"),
            Map.entry("keio", "京王電鉄"),
            Map.entry("keisei", "京成電鉄"),
            Map.entry("keikyu", "京浜急行電鉄"),
            Map.entry("odakyu", "小田急電鉄"),
            Map.entry("tokyu", "東急電鉄"),
            Map.entry("tobu", "東武鉄道"),
            Map.entry("seibu", "西武鉄道"),
            Map.entry("hokuso", "北総鉄道"),
            Map.entry("hokuso railway", "北総鉄道"),
            Map.entry("saitama railway", "埼玉高速鉄道"),
            Map.entry("tsukuba express", "首都圏新都市鉄道"),
            Map.entry("mitake tozan railway", "御岳登山鉄道"),
            Map.entry("takao tozan railway", "高尾登山電鉄"),
            Map.entry("tokyo monorail", "東京モノレール"),
            Map.entry("tama monorail", "多摩都市モノレール"),
            Map.entry("tokyo waterfront area rapid transit", "東京臨海高速鉄道"),
            Map.entry("yurikamome", "ゆりかもめ")
    );

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public TokyoRailCzmlExportService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public void exportDefault(Path outputPath) throws IOException, InterruptedException {
        exportCzml(outputPath, 0.0, DEFAULT_OPERATOR_FILTERS);
    }

    public void exportCzml(Path outputPath, double height, List<String> operatorFilters)
            throws IOException, InterruptedException {
        List<String> resolvedOperatorFilters = normalizeOperatorFilters(operatorFilters);
        Set<String> normalizedOperatorFilterKeys = new LinkedHashSet<>();
        for (String operator : resolvedOperatorFilters) {
            normalizedOperatorFilterKeys.add(normalizeOperatorName(operator));
        }

        JsonNode railroadGeoJson = fetchJsonFromZip(RAILROAD_DATASET_URL, RAILROAD_GEOJSON_PATH);
        JsonNode prefectureGeoJson = fetchJsonFromZip(TOKYO.url(), TOKYO.geoJsonPath());
        List<JsonNode> matchedFeatures = filterRailSections(
                railroadGeoJson,
                prefectureGeoJson,
                normalizedOperatorFilterKeys.isEmpty() ? null : normalizedOperatorFilterKeys
        );

        ArrayNode czml = objectMapper.createArrayNode();
        czml.add(buildDocumentPacket(resolvedOperatorFilters));

        int sectionIndex = 1;
        for (JsonNode feature : matchedFeatures) {
            JsonNode properties = feature.path("properties");
            JsonNode coordinates = feature.path("geometry").path("coordinates");

            String operatorName = properties.path("N02_004").asText("");
            String lineName = properties.path("N02_003").asText("");
            String sectionId = TOKYO.key() + "-" + sectionIndex++;

            czml.add(buildSectionPacket(sectionId, operatorName, lineName, coordinates, height));
        }

        Files.createDirectories(outputPath.toAbsolutePath().getParent());
        objectMapper.writeValue(outputPath.toFile(), czml);
    }

    public static void main(String[] args) throws Exception {
        Path outputPath = args.length > 0
                ? Path.of(args[0])
                : Path.of("line_export_tool/rail_lines_tokyo_java.czml");
        double height = args.length > 1 ? Double.parseDouble(args[1]) : 0.0;
        List<String> operatorFilters = args.length > 2
                ? Arrays.asList(Arrays.copyOfRange(args, 2, args.length))
                : DEFAULT_OPERATOR_FILTERS;

        TokyoRailCzmlExportService service = new TokyoRailCzmlExportService();
        service.exportCzml(outputPath, height, operatorFilters);

        System.out.println("CZML exported to: " + outputPath.toAbsolutePath());
        System.out.println("Operator filters: " + operatorFilters);
    }

    private JsonNode fetchJsonFromZip(String zipUrl, String memberPath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(zipUrl)).GET().build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Failed to fetch " + zipUrl + " (HTTP " + response.statusCode() + ")");
        }

        try (InputStream body = response.body(); ZipInputStream zipInputStream = new ZipInputStream(body)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (memberPath.equals(entry.getName())) {
                    return objectMapper.readTree(zipInputStream);
                }
            }
        }

        throw new IOException("ZIP member not found: " + memberPath + " in " + zipUrl);
    }

    private List<String> normalizeOperatorFilters(List<String> requested) {
        List<String> normalized = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (String item : requested == null ? List.<String>of() : requested) {
            String resolved = resolveOperatorFilter(item);
            if (resolved.isBlank()) {
                throw new IllegalArgumentException("Operator filter cannot be empty.");
            }
            String key = normalizeOperatorName(resolved);
            if (seen.add(key)) {
                normalized.add(resolved);
            }
        }

        return normalized;
    }

    private String resolveOperatorFilter(String value) {
        String trimmed = value == null ? "" : value.trim();
        return OPERATOR_FILTER_ALIASES.getOrDefault(normalizeOperatorName(trimmed), trimmed);
    }

    private String normalizeOperatorName(String value) {
        return value == null
                ? ""
                : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private ObjectNode buildDocumentPacket(List<String> operatorFilters) {
        ObjectNode document = objectMapper.createObjectNode();
        document.put("id", "document");
        document.put("version", "1.0");
        document.put("name", "Tokyo Rail Lines");

        ObjectNode properties = document.putObject("properties");
        properties.put("format", "cesium-rail-lines-czml-v1");
        ArrayNode prefectures = properties.putArray("generated_prefectures");
        prefectures.add(TOKYO.key());
        properties.put("prefecture_count", 1);
        ArrayNode operatorFiltersEn = properties.putArray("operator_filters_en");
        for (String operatorFilter : operatorFilters) {
            operatorFiltersEn.add(operatorNameEn(operatorFilter).orElse(operatorFilter));
        }

        return document;
    }

    private ObjectNode buildSectionPacket(
            String sectionId,
            String operatorName,
            String lineName,
            JsonNode coordinates,
            double height
    ) {
        ObjectNode packet = objectMapper.createObjectNode();
        packet.put("id", sectionId);

        ObjectNode polyline = packet.putObject("polyline");
        ObjectNode positions = polyline.putObject("positions");
        positions.set("cartographicDegrees", flattenCartographicDegrees(coordinates, height));

        int[] rgba = lineColorRgba(operatorName, lineName);
        ObjectNode material = polyline.putObject("material");
        ObjectNode solidColor = material.putObject("solidColor");
        ArrayNode color = solidColor.putObject("color").putArray("rgba");
        color.add(rgba[0]);
        color.add(rgba[1]);
        color.add(rgba[2]);
        color.add(rgba[3]);
        polyline.put("width", 3);

        return packet;
    }

    private ArrayNode flattenCartographicDegrees(JsonNode coordinates, double height) {
        ArrayNode flattened = objectMapper.createArrayNode();
        for (JsonNode point : coordinates) {
            flattened.add(round6(point.get(0).asDouble()));
            flattened.add(round6(point.get(1).asDouble()));
            flattened.add(height);
        }
        return flattened;
    }

    private int[] lineColorRgba(String operatorName, String lineName) {
        String seedValue = (operatorName == null ? "unknown" : operatorName)
                + "|"
                + (lineName == null ? "unknown" : lineName);
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-1").digest(seedValue.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 not available", exception);
        }

        return new int[] {
                64 + Byte.toUnsignedInt(digest[0]) % 160,
                64 + Byte.toUnsignedInt(digest[1]) % 160,
                64 + Byte.toUnsignedInt(digest[2]) % 160,
                255
        };
    }

    private List<JsonNode> filterRailSections(
            JsonNode railroadGeoJson,
            JsonNode prefectureGeoJson,
            Set<String> operatorFilters
    ) {
        List<JsonNode> prefectureGeometries = new ArrayList<>();
        List<BBox> prefectureBboxes = new ArrayList<>();

        for (JsonNode feature : prefectureGeoJson.path("features")) {
            JsonNode geometry = feature.path("geometry");
            BBox bbox = geometryBBox(geometry);
            if (bbox == null) {
                continue;
            }
            prefectureGeometries.add(geometry);
            prefectureBboxes.add(bbox);
        }

        if (prefectureBboxes.isEmpty()) {
            return List.of();
        }

        BBox overallBbox = combineBBoxes(prefectureBboxes);
        List<JsonNode> matches = new ArrayList<>();

        for (JsonNode feature : railroadGeoJson.path("features")) {
            JsonNode geometry = feature.path("geometry");
            if (!"LineString".equals(geometry.path("type").asText())) {
                continue;
            }

            JsonNode properties = feature.path("properties");
            String operatorName = properties.path("N02_004").asText("");
            if (operatorFilters != null && !operatorFilters.contains(normalizeOperatorName(operatorName))) {
                continue;
            }

            JsonNode coordinates = geometry.path("coordinates");
            if (!coordinates.isArray() || coordinates.size() < 2) {
                continue;
            }

            BBox lineBBox = lineBBox(coordinates);
            if (!bboxIntersects(lineBBox, overallBbox)) {
                continue;
            }

            boolean matchesPrefecture = false;
            for (int i = 0; i < prefectureGeometries.size(); i += 1) {
                if (bboxIntersects(lineBBox, prefectureBboxes.get(i))
                        && lineIntersectsGeometry(coordinates, prefectureGeometries.get(i))) {
                    matchesPrefecture = true;
                    break;
                }
            }

            if (matchesPrefecture) {
                matches.add(feature);
            }
        }

        return matches;
    }

    private boolean lineIntersectsGeometry(JsonNode coordinates, JsonNode geometry) {
        for (JsonNode point : coordinates) {
            if (pointInGeometry(point.get(0).asDouble(), point.get(1).asDouble(), geometry)) {
                return true;
            }
        }
        return false;
    }

    private boolean pointInGeometry(double x, double y, JsonNode geometry) {
        String geometryType = geometry.path("type").asText();
        JsonNode coordinates = geometry.path("coordinates");

        if ("Polygon".equals(geometryType)) {
            return pointInPolygon(x, y, coordinates);
        }
        if ("MultiPolygon".equals(geometryType)) {
            for (JsonNode polygon : coordinates) {
                if (pointInPolygon(x, y, polygon)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean pointInPolygon(double x, double y, JsonNode polygonCoordinates) {
        if (!polygonCoordinates.isArray() || polygonCoordinates.isEmpty()) {
            return false;
        }
        if (!pointInRing(x, y, polygonCoordinates.get(0))) {
            return false;
        }
        for (int i = 1; i < polygonCoordinates.size(); i += 1) {
            if (pointInRing(x, y, polygonCoordinates.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean pointInRing(double x, double y, JsonNode ring) {
        boolean inside = false;
        int j = ring.size() - 1;
        for (int i = 0; i < ring.size(); i += 1) {
            double xi = ring.get(i).get(0).asDouble();
            double yi = ring.get(i).get(1).asDouble();
            double xj = ring.get(j).get(0).asDouble();
            double yj = ring.get(j).get(1).asDouble();
            boolean intersects = ((yi > y) != (yj > y))
                    && (x < (xj - xi) * (y - yi) / ((yj - yi) == 0 ? 1e-12 : (yj - yi)) + xi);
            if (intersects) {
                inside = !inside;
            }
            j = i;
        }
        return inside;
    }

    private BBox combineBBoxes(List<BBox> bboxes) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (BBox bbox : bboxes) {
            minX = Math.min(minX, bbox.minX());
            minY = Math.min(minY, bbox.minY());
            maxX = Math.max(maxX, bbox.maxX());
            maxY = Math.max(maxY, bbox.maxY());
        }

        return new BBox(minX, minY, maxX, maxY);
    }

    private BBox geometryBBox(JsonNode geometry) {
        List<JsonNode> rings = new ArrayList<>();
        collectPolygonRings(geometry, rings);
        if (rings.isEmpty()) {
            return null;
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (JsonNode ring : rings) {
            for (JsonNode point : ring) {
                double x = point.get(0).asDouble();
                double y = point.get(1).asDouble();
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        return new BBox(minX, minY, maxX, maxY);
    }

    private void collectPolygonRings(JsonNode geometry, List<JsonNode> rings) {
        String geometryType = geometry.path("type").asText();
        JsonNode coordinates = geometry.path("coordinates");

        if ("Polygon".equals(geometryType)) {
            coordinates.forEach(rings::add);
            return;
        }
        if ("MultiPolygon".equals(geometryType)) {
            for (JsonNode polygon : coordinates) {
                polygon.forEach(rings::add);
            }
        }
    }

    private BBox lineBBox(JsonNode coordinates) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (JsonNode point : coordinates) {
            double x = point.get(0).asDouble();
            double y = point.get(1).asDouble();
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        return new BBox(minX, minY, maxX, maxY);
    }

    private boolean bboxIntersects(BBox a, BBox b) {
        return !(a.maxX() < b.minX()
                || b.maxX() < a.minX()
                || a.maxY() < b.minY()
                || b.maxY() < a.minY());
    }

    private double round6(double value) {
        return Math.round(value * 1_000_000d) / 1_000_000d;
    }

    private java.util.Optional<String> operatorNameEn(String operatorName) {
        return java.util.Optional.ofNullable(OPERATOR_NAME_EN.get(operatorName));
    }

    private record PrefectureDataset(
            String key,
            String name,
            String jpName,
            String code,
            String url,
            String geoJsonPath
    ) {
    }

    private record BBox(double minX, double minY, double maxX, double maxY) {
    }
}
