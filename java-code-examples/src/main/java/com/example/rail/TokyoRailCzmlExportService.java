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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Exports Tokyo and Kanagawa rail sections to lightweight CZML.
 *
 * <p>This mirrors the current Python exporter:
 * - Tokyo and Kanagawa boundaries
 * - optional operator filtering using English aliases such as "Toei"
 * - one CZML polyline packet per rail section
 * - stable color per line
 */
public class TokyoRailCzmlExportService {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public TokyoRailCzmlExportService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    public void exportDefault(Path outputPath) throws IOException, InterruptedException {
        exportCzml(outputPath, 0.0, TokyoRailCzmlExportConfig.DEFAULT_OPERATOR_FILTERS);
    }

    public void exportCzml(Path outputPath, double height, List<String> operatorFilters)
            throws IOException, InterruptedException {
        Path cacheDirectory = resolveCacheDirectoryForFile(outputPath);
        ExportData exportData = loadExportData(operatorFilters, cacheDirectory);

        ArrayNode czml = objectMapper.createArrayNode();
        czml.add(buildDocumentPacket(exportData.generatedPrefectureKeys(), exportData.resolvedOperatorFilters()));

        int sectionIndex = 1;
        for (RailSectionFeature feature : exportData.features()) {
            String sectionId = "section-" + sectionIndex++;
            czml.add(buildSectionPacket(
                    sectionId,
                    feature.operatorName(),
                    feature.lineName(),
                    feature.coordinates(),
                    height
            ));
        }

        Files.createDirectories(outputPath.toAbsolutePath().getParent());
        objectMapper.writeValue(outputPath.toFile(), czml);
    }

    public List<Path> exportCzmlPerLine(Path outputDirectory, double height, List<String> operatorFilters)
            throws IOException, InterruptedException {
        Path cacheDirectory = resolveCacheDirectoryForDirectory(outputDirectory);
        ExportData exportData = loadExportData(operatorFilters, cacheDirectory);
        Map<LineKey, List<RailSectionFeature>> groupedByLine = new LinkedHashMap<>();

        for (RailSectionFeature feature : exportData.features()) {
            LineKey lineKey = new LineKey(feature.operatorName(), feature.lineName());
            groupedByLine.computeIfAbsent(lineKey, unused -> new ArrayList<>()).add(feature);
        }

        Files.createDirectories(outputDirectory);
        List<Path> createdFiles = new ArrayList<>();
        int fileIndex = 1;

        for (Map.Entry<LineKey, List<RailSectionFeature>> entry : groupedByLine.entrySet()) {
            LineKey lineKey = entry.getKey();
            ArrayNode czml = objectMapper.createArrayNode();
            czml.add(buildDocumentPacket(
                    exportData.generatedPrefectureKeys(),
                    exportData.resolvedOperatorFilters(),
                    lineKey.operatorName(),
                    lineKey.lineName()
            ));

            int sectionIndex = 1;
            for (RailSectionFeature feature : entry.getValue()) {
                czml.add(buildSectionPacket(
                        buildSectionId(lineKey, sectionIndex++),
                        feature.operatorName(),
                        feature.lineName(),
                        feature.coordinates(),
                        height
                ));
            }

            Path outputPath = outputDirectory.resolve(buildLineFileName(fileIndex++, lineKey));
            objectMapper.writeValue(outputPath.toFile(), czml);
            createdFiles.add(outputPath);
        }

        return createdFiles;
    }

    private ExportData loadExportData(List<String> operatorFilters, Path cacheDirectory)
            throws IOException, InterruptedException {
        List<String> resolvedOperatorFilters = normalizeOperatorFilters(operatorFilters);
        Set<String> normalizedOperatorFilterKeys = new LinkedHashSet<>();
        for (String operator : resolvedOperatorFilters) {
            normalizedOperatorFilterKeys.add(normalizeOperatorName(operator));
        }

        JsonNode railroadGeoJson = fetchJsonFromZip(
                TokyoRailCzmlExportConfig.RAILROAD_DATASET_URL,
                TokyoRailCzmlExportConfig.RAILROAD_GEOJSON_PATH,
                cacheDirectory
        );
        List<PrefectureArea> prefectureAreas = new ArrayList<>();
        List<String> generatedPrefectureKeys = new ArrayList<>();
        for (TokyoRailCzmlExportConfig.PrefectureDataset prefecture : TokyoRailCzmlExportConfig.PREFECTURES) {
            JsonNode prefectureGeoJson = fetchJsonFromZip(
                    prefecture.url(),
                    prefecture.geoJsonPath(),
                    cacheDirectory
            );
            generatedPrefectureKeys.add(prefecture.key());
            collectPrefectureAreas(prefectureGeoJson, prefectureAreas);
        }
        List<JsonNode> matchedFeatures = filterRailSections(
                railroadGeoJson,
                prefectureAreas,
                normalizedOperatorFilterKeys.isEmpty() ? null : normalizedOperatorFilterKeys
        );

        List<RailSectionFeature> features = new ArrayList<>();
        for (JsonNode feature : matchedFeatures) {
            JsonNode properties = feature.path("properties");
            features.add(new RailSectionFeature(
                    properties.path("N02_004").asText(""),
                    properties.path("N02_003").asText(""),
                    feature.path("geometry").path("coordinates")
            ));
        }
        return new ExportData(generatedPrefectureKeys, resolvedOperatorFilters, features);
    }

    private JsonNode fetchJsonFromZip(String zipUrl, String memberPath, Path cacheDirectory)
            throws IOException, InterruptedException {
        Files.createDirectories(cacheDirectory);
        Path cachedZipPath = cacheDirectory.resolve(cacheFileNameFromUrl(zipUrl));

        if (!Files.exists(cachedZipPath)) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(zipUrl)).GET().build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Failed to fetch " + zipUrl + " (HTTP " + response.statusCode() + ")");
            }
            try (InputStream body = response.body()) {
                Files.copy(body, cachedZipPath);
            }
        }

        try (InputStream fileInputStream = Files.newInputStream(cachedZipPath);
             ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (memberPath.equals(entry.getName())) {
                    return objectMapper.readTree(zipInputStream);
                }
            }
        }

        throw new IOException("ZIP member not found: " + memberPath + " in " + cachedZipPath);
    }

    private Path resolveCacheDirectoryForFile(Path outputPath) {
        Path absolutePath = outputPath.toAbsolutePath();
        Path parent = absolutePath.getParent();
        return parent == null ? Path.of(".").toAbsolutePath() : parent;
    }

    private Path resolveCacheDirectoryForDirectory(Path outputDirectory) {
        return outputDirectory.toAbsolutePath();
    }

    private String cacheFileNameFromUrl(String zipUrl) {
        String path = URI.create(zipUrl).getPath();
        int slashIndex = path.lastIndexOf('/');
        return slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
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
        return TokyoRailCzmlExportConfig.OPERATOR_FILTER_ALIASES.getOrDefault(
                normalizeOperatorName(trimmed),
                trimmed
        );
    }

    private String normalizeOperatorName(String value) {
        return value == null
                ? ""
                : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private ObjectNode buildDocumentPacket(List<String> prefectureKeys, List<String> operatorFilters) {
        return buildDocumentPacket(prefectureKeys, operatorFilters, null, null);
    }

    private ObjectNode buildDocumentPacket(
            List<String> prefectureKeys,
            List<String> operatorFilters,
            String operatorName,
            String lineName
    ) {
        ObjectNode document = objectMapper.createObjectNode();
        document.put("id", "document");
        document.put("version", "1.0");
        document.put("name", buildDocumentName(prefectureKeys, operatorName, lineName));

        ObjectNode properties = document.putObject("properties");
        properties.put("format", "cesium-rail-lines-czml-v1");
        ArrayNode prefectures = properties.putArray("generated_prefectures");
        for (String prefectureKey : prefectureKeys) {
            prefectures.add(prefectureKey);
        }
        properties.put("prefecture_count", prefectureKeys.size());
        if (!isBlank(operatorName)) {
            properties.put("operator_name", operatorName);
            properties.put("operator_name_en", operatorNameEn(operatorName).orElse(operatorName));
        }
        if (!isBlank(lineName)) {
            properties.put("line_name", lineName);
        }
        ArrayNode operatorFiltersEn = properties.putArray("operator_filters_en");
        for (String operatorFilter : operatorFilters) {
            operatorFiltersEn.add(operatorNameEn(operatorFilter).orElse(operatorFilter));
        }

        return document;
    }

    private String buildDocumentName(List<String> prefectureKeys, String operatorName, String lineName) {
        if (isBlank(lineName)) {
            return buildPrefectureLabel(prefectureKeys) + " Rail Lines";
        }
        String operatorDisplay = isBlank(operatorName)
                ? "Unknown operator"
                : operatorNameEn(operatorName).orElse(operatorName);
        return operatorDisplay + " / " + lineName;
    }

    private String buildPrefectureLabel(List<String> prefectureKeys) {
        List<String> prefectureNames = new ArrayList<>();
        for (String prefectureKey : prefectureKeys) {
            for (TokyoRailCzmlExportConfig.PrefectureDataset prefecture : TokyoRailCzmlExportConfig.PREFECTURES) {
                if (prefecture.key().equals(prefectureKey)) {
                    prefectureNames.add(prefecture.name());
                    break;
                }
            }
        }
        return prefectureNames.isEmpty() ? "Japan" : String.join(" + ", prefectureNames);
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

    private String buildLineFileName(int index, LineKey lineKey) {
        String operatorComponent = sanitizeFileNameComponent(
                operatorNameEn(lineKey.operatorName()).orElse(lineKey.operatorName())
        );
        String lineComponent = sanitizeFileNameComponent(
                lineNameEn(lineKey.lineName()).orElse(lineKey.lineName())
        );
        return String.format("%03d_%s_%s.czml", index, operatorComponent, lineComponent);
    }

    private String buildSectionId(LineKey lineKey, int sectionIndex) {
        return sanitizeIdentifier(lineKey.operatorName())
                + "-"
                + sanitizeIdentifier(lineKey.lineName())
                + "-"
                + sectionIndex;
    }

    private String sanitizeFileNameComponent(String value) {
        String sanitized = value == null ? "" : value.trim();
        sanitized = sanitized.replaceAll("[\\\\/:*?\"<>|]+", "_");
        sanitized = sanitized.replaceAll("\\s+", "_");
        sanitized = sanitized.replaceAll("_+", "_");
        sanitized = sanitized.replaceAll("^_+|_+$", "");
        return sanitized.isBlank() ? "unknown" : sanitized;
    }

    private String sanitizeIdentifier(String value) {
        return sanitizeFileNameComponent(value).toLowerCase(Locale.ROOT);
    }

    private List<JsonNode> filterRailSections(
            JsonNode railroadGeoJson,
            List<PrefectureArea> prefectureAreas,
            Set<String> operatorFilters
    ) {
        if (prefectureAreas.isEmpty()) {
            return List.of();
        }

        BBox overallBbox = combineBBoxes(prefectureAreas.stream().map(PrefectureArea::bbox).toList());
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
            for (PrefectureArea prefectureArea : prefectureAreas) {
                if (bboxIntersects(lineBBox, prefectureArea.bbox())
                        && lineIntersectsGeometry(coordinates, prefectureArea.geometry())) {
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

    private void collectPrefectureAreas(JsonNode prefectureGeoJson, List<PrefectureArea> prefectureAreas) {
        for (JsonNode feature : prefectureGeoJson.path("features")) {
            JsonNode geometry = feature.path("geometry");
            BBox bbox = geometryBBox(geometry);
            if (bbox != null) {
                prefectureAreas.add(new PrefectureArea(geometry, bbox));
            }
        }
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private java.util.Optional<String> operatorNameEn(String operatorName) {
        return java.util.Optional.ofNullable(TokyoRailCzmlExportConfig.OPERATOR_NAME_EN.get(operatorName));
    }

    private java.util.Optional<String> lineNameEn(String lineName) {
        return java.util.Optional.ofNullable(TokyoRailCzmlExportConfig.LINE_NAME_EN.get(lineName));
    }

    private record BBox(double minX, double minY, double maxX, double maxY) {
    }

    private record PrefectureArea(JsonNode geometry, BBox bbox) {
    }

    private record RailSectionFeature(String operatorName, String lineName, JsonNode coordinates) {
    }

    private record ExportData(
            List<String> generatedPrefectureKeys,
            List<String> resolvedOperatorFilters,
            List<RailSectionFeature> features
    ) {
    }

    private record LineKey(String operatorName, String lineName) {
    }
}
