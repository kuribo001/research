import com.twins.crawler.dtos.EarthquakeComment;
import com.twins.crawler.dtos.EarthquakeCountItem;
import com.twins.crawler.dtos.EarthquakeEvent;
import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import com.twins.crawler.dtos.EarthquakeHypocenter;
import com.twins.crawler.dtos.EarthquakeIntensityArea;
import com.twins.crawler.dtos.EarthquakeMunicipalityIntensity;
import com.twins.crawler.dtos.EarthquakeNoticeItem;
import com.twins.crawler.dtos.EarthquakeSpecialInformation;
import com.twins.crawler.dtos.EarthquakeSpecialTextBlock;
import com.twins.crawler.dtos.EarthquakeStationIntensity;
import com.twins.crawler.dtos.EewDetail;
import com.twins.crawler.dtos.EewForecastArea;
import com.twins.crawler.dtos.LongPeriodStationMetric;
import com.twins.crawler.dtos.ReportHead;
import com.twins.crawler.parsers.earthquake.EarthquakeParsingService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EarthquakeXmlIngestExample {
    private static final Pattern MESSAGE_CODE_PATTERN = Pattern.compile("(V[XYZ]SE\\d{2})");

    private EarthquakeXmlIngestExample() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            printUsage();
            return;
        }

        String xmlUrl = args[0];
        String jdbcUrl = args[1];
        String dbUser = args[2];
        String dbPassword = args[3];
        String explicitMessageCode = args.length >= 5 ? args[4] : null;

        String xml = downloadXml(xmlUrl);
        String messageCode = explicitMessageCode != null ? explicitMessageCode : inferMessageCode(xmlUrl);
        EarthquakeEventEnvelope envelope = EarthquakeParsingService.parse(messageCode, xml);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setAutoCommit(false);
            try {
                long earthquakeEventId = upsertEventSnapshot(connection, envelope.event());
                saveSingletons(connection, earthquakeEventId, envelope);
                saveCollectionTables(connection, earthquakeEventId, envelope);
                connection.commit();
                System.out.println("Saved earthquake event id=" + earthquakeEventId
                    + " messageCode=" + messageCode
                    + " eventId=" + envelope.event().head().eventId());
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java EarthquakeXmlIngestExample <xmlUrl> <jdbcUrl> <dbUser> <dbPassword> [messageCode]");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java EarthquakeXmlIngestExample \\");
        System.out.println("    https://example.com/20260426202909_0_VXSE53_010000.xml \\");
        System.out.println("    jdbc:postgresql://localhost:5432/jma jma_user secret VXSE53");
    }

    private static String downloadXml(String xmlUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(xmlUrl)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Failed to download XML: HTTP " + response.statusCode());
        }
        return response.body();
    }

    private static String inferMessageCode(String text) {
        Matcher matcher = MESSAGE_CODE_PATTERN.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not infer JMA message code from: " + text);
        }
        return matcher.group(1);
    }

    private static long upsertEventSnapshot(Connection connection, EarthquakeEvent event) throws SQLException {
        ReportHead head = event.head();
        Long existingId = findExistingEventId(connection, head.eventId(), event.messageCode(), head.issuedAt(), event.serial());
        if (existingId != null) {
            updateEventSnapshot(connection, existingId, event);
            return existingId;
        }

        String sql = """
            INSERT INTO jma_earthquake_event (
                event_id, entry_id, message_code, family_name, title, info_kind, info_type, serial,
                issued_at, target_date_time, event_time, event_time_utc, event_time_precision,
                headline_text, next_advisory, magnitude, magnitude_type, max_intensity,
                domestic_tsunami, foreign_tsunami, is_cancelled
            ) VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, head.eventId());
            statement.setString(index++, event.messageCode());
            statement.setString(index++, event.familyName());
            statement.setString(index++, event.title());
            statement.setString(index++, head.infoKind());
            statement.setString(index++, head.infoType());
            setInteger(statement, index++, event.serial());
            setOffsetDateTime(statement, index++, head.issuedAt());
            setOffsetDateTime(statement, index++, event.targetDateTime());
            setOffsetDateTime(statement, index++, event.eventAt());
            setOffsetDateTime(statement, index++, event.eventAtUtc());
            statement.setString(index++, event.eventTimePrecision());
            statement.setString(index++, event.headlineText());
            statement.setString(index++, event.nextAdvisory());
            setDouble(statement, index++, event.magnitude());
            statement.setString(index++, event.magnitudeType());
            statement.setString(index++, event.maxIntensity());
            statement.setString(index++, event.domesticTsunami());
            statement.setString(index++, event.foreignTsunami());
            statement.setBoolean(index, event.cancelled());

            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static Long findExistingEventId(
        Connection connection,
        String eventId,
        String messageCode,
        OffsetDateTime issuedAt,
        Integer serial
    ) throws SQLException {
        String sql = """
            SELECT id
            FROM jma_earthquake_event
            WHERE event_id = ?
              AND message_code = ?
              AND issued_at IS NOT DISTINCT FROM ?
              AND serial IS NOT DISTINCT FROM ?
            ORDER BY id DESC
            LIMIT 1
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, eventId);
            statement.setString(2, messageCode);
            setOffsetDateTime(statement, 3, issuedAt);
            setInteger(statement, 4, serial);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private static void updateEventSnapshot(Connection connection, long eventId, EarthquakeEvent event) throws SQLException {
        ReportHead head = event.head();
        String sql = """
            UPDATE jma_earthquake_event
            SET family_name = ?, title = ?, info_kind = ?, info_type = ?, serial = ?, issued_at = ?,
                target_date_time = ?, event_time = ?, event_time_utc = ?, event_time_precision = ?,
                headline_text = ?, next_advisory = ?, magnitude = ?, magnitude_type = ?, max_intensity = ?,
                domestic_tsunami = ?, foreign_tsunami = ?, is_cancelled = ?, updated_at = now()
            WHERE id = ?
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, event.familyName());
            statement.setString(index++, event.title());
            statement.setString(index++, head.infoKind());
            statement.setString(index++, head.infoType());
            setInteger(statement, index++, event.serial());
            setOffsetDateTime(statement, index++, head.issuedAt());
            setOffsetDateTime(statement, index++, event.targetDateTime());
            setOffsetDateTime(statement, index++, event.eventAt());
            setOffsetDateTime(statement, index++, event.eventAtUtc());
            statement.setString(index++, event.eventTimePrecision());
            statement.setString(index++, event.headlineText());
            statement.setString(index++, event.nextAdvisory());
            setDouble(statement, index++, event.magnitude());
            statement.setString(index++, event.magnitudeType());
            statement.setString(index++, event.maxIntensity());
            statement.setString(index++, event.domesticTsunami());
            statement.setString(index++, event.foreignTsunami());
            statement.setBoolean(index++, event.cancelled());
            statement.setLong(index, eventId);
            statement.executeUpdate();
        }
    }

    private static void saveSingletons(Connection connection, long eventId, EarthquakeEventEnvelope envelope) throws SQLException {
        upsertHypocenter(connection, eventId, envelope.hypocenter());
        upsertComment(connection, eventId, envelope.comment());
        upsertEewDetail(connection, eventId, envelope.eewDetail());
        upsertSpecialInformation(connection, eventId, envelope.specialInformation());
    }

    private static void saveCollectionTables(Connection connection, long eventId, EarthquakeEventEnvelope envelope) throws SQLException {
        deleteByEventId(connection, "jma_earthquake_intensity_area", eventId);
        deleteByEventId(connection, "jma_earthquake_municipality_intensity", eventId);
        deleteByEventId(connection, "jma_earthquake_station_intensity", eventId);
        deleteByEventId(connection, "jma_eew_forecast_area", eventId);
        deleteByEventId(connection, "jma_earthquake_special_text_block", eventId);
        deleteByEventId(connection, "jma_earthquake_notice_item", eventId);
        deleteByEventId(connection, "jma_earthquake_count_item", eventId);

        Map<String, Long> stationIdByCode = insertStationIntensities(connection, eventId, envelope);
        insertIntensityAreas(connection, eventId, envelope);
        insertMunicipalityIntensities(connection, eventId, envelope);
        insertLongPeriodMetrics(connection, stationIdByCode, envelope);
        insertEewForecastAreas(connection, eventId, envelope);
        insertSpecialTextBlocks(connection, eventId, envelope);
        insertNoticeItems(connection, eventId, envelope);
        insertCountItems(connection, eventId, envelope);
    }

    private static void upsertHypocenter(Connection connection, long eventId, EarthquakeHypocenter hypocenter) throws SQLException {
        deleteSingleton(connection, "jma_earthquake_hypocenter", eventId);
        if (hypocenter == null) {
            return;
        }

        String sql = """
            INSERT INTO jma_earthquake_hypocenter (
                earthquake_event_id, area_name, area_code, reduce_name, reduce_code, land_or_sea,
                coordinate_raw, latitude, longitude, depth_m, depth_condition, epicenter_accuracy_rank,
                epicenter_accuracy_rank2, depth_accuracy_rank, magnitude_calculation_rank,
                number_of_magnitude_calculation, magnitude, magnitude_type, textual_description
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setLong(index++, eventId);
            statement.setString(index++, hypocenter.areaName());
            statement.setString(index++, hypocenter.areaCode());
            statement.setString(index++, hypocenter.reduceName());
            statement.setString(index++, hypocenter.reduceCode());
            statement.setString(index++, hypocenter.landOrSea());
            statement.setString(index++, hypocenter.coordinateRaw());
            setDouble(statement, index++, hypocenter.latitude());
            setDouble(statement, index++, hypocenter.longitude());
            setInteger(statement, index++, hypocenter.depthM());
            statement.setString(index++, hypocenter.depthCondition());
            statement.setString(index++, hypocenter.epicenterAccuracyRank());
            statement.setString(index++, hypocenter.epicenterAccuracyRank2());
            statement.setString(index++, hypocenter.depthAccuracyRank());
            statement.setString(index++, hypocenter.magnitudeCalculationRank());
            setInteger(statement, index++, hypocenter.numberOfMagnitudeCalculation());
            setDouble(statement, index++, hypocenter.magnitude());
            statement.setString(index++, hypocenter.magnitudeType());
            statement.setString(index, hypocenter.textualDescription());
            statement.executeUpdate();
        }
    }

    private static void upsertComment(Connection connection, long eventId, EarthquakeComment comment) throws SQLException {
        deleteSingleton(connection, "jma_earthquake_comment", eventId);
        if (comment == null) {
            return;
        }

        String sql = """
            INSERT INTO jma_earthquake_comment (
                earthquake_event_id, body_text, free_text, additional_text,
                tsunami_comment, forecast_comment, appendix, warning_comment_text, warning_comment_code
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, eventId);
            statement.setString(2, comment.bodyText());
            statement.setString(3, comment.freeText());
            statement.setString(4, comment.additionalText());
            statement.setString(5, comment.tsunamiComment());
            statement.setString(6, comment.forecastComment());
            statement.setString(7, comment.appendix());
            statement.setString(8, comment.warningCommentText());
            statement.setString(9, comment.warningCommentCode());
            statement.executeUpdate();
        }
    }

    private static void upsertEewDetail(Connection connection, long eventId, EewDetail detail) throws SQLException {
        deleteSingleton(connection, "jma_eew_detail", eventId);
        if (detail == null) {
            return;
        }

        String sql = """
            INSERT INTO jma_eew_detail (
                earthquake_event_id, report_num, is_last_report, is_plum_assumption,
                trigger_origin_time, trigger_arrival_time, forecast_max_int_change,
                forecast_max_lg_int_change, forecast_max_int_change_reason, warning_text,
                warning_code, textual_forecast
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, eventId);
            setInteger(statement, 2, detail.reportNum());
            statement.setBoolean(3, detail.lastReport());
            statement.setBoolean(4, detail.plumAssumption());
            setOffsetDateTime(statement, 5, detail.triggerOriginTime());
            setOffsetDateTime(statement, 6, detail.triggerArrivalTime());
            statement.setString(7, detail.forecastMaxIntChange());
            statement.setString(8, detail.forecastMaxLgIntChange());
            statement.setString(9, detail.forecastMaxIntChangeReason());
            statement.setString(10, detail.warningText());
            statement.setString(11, detail.warningCode());
            statement.setString(12, detail.textualForecast());
            statement.executeUpdate();
        }
    }

    private static void upsertSpecialInformation(Connection connection, long eventId, EarthquakeSpecialInformation info) throws SQLException {
        deleteSingleton(connection, "jma_earthquake_special_information", eventId);
        if (info == null) {
            return;
        }

        String sql = """
            INSERT INTO jma_earthquake_special_information (
                earthquake_event_id, information_name, information_keyword, information_code,
                serial_name, serial_code, report_condition, advisory_type, validity_text
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, eventId);
            statement.setString(2, info.informationName());
            statement.setString(3, info.informationKeyword());
            statement.setString(4, info.informationCode());
            statement.setString(5, info.serialName());
            statement.setString(6, info.serialCode());
            statement.setString(7, info.reportCondition());
            statement.setString(8, info.advisoryType());
            statement.setString(9, info.validityText());
            statement.executeUpdate();
        }
    }

    private static void insertIntensityAreas(Connection connection, long eventId, EarthquakeEventEnvelope envelope) throws SQLException {
        String sql = """
            INSERT INTO jma_earthquake_intensity_area (
                earthquake_event_id, source_type, area_name, area_code,
                intensity, intensity_numeric, long_period_class, sort_order
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (EarthquakeIntensityArea area : envelope.intensityAreas()) {
                statement.setLong(1, eventId);
                statement.setString(2, area.sourceType());
                statement.setString(3, area.areaName());
                statement.setString(4, area.areaCode());
                statement.setString(5, area.intensity());
                setDouble(statement, 6, area.intensityNumeric());
                statement.setString(7, area.longPeriodClass());
                setInteger(statement, 8, area.sortOrder());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertMunicipalityIntensities(Connection connection, long eventId, EarthquakeEventEnvelope envelope) throws SQLException {
        String sql = """
            INSERT INTO jma_earthquake_municipality_intensity (
                earthquake_event_id, source_type, prefecture_name, prefecture_code,
                area_name, area_code, city_name, city_code, intensity,
                intensity_numeric, long_period_class, maximum_flag, revise_type
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (EarthquakeMunicipalityIntensity item : envelope.municipalityIntensities()) {
                statement.setLong(1, eventId);
                statement.setString(2, item.sourceType());
                statement.setString(3, item.prefectureName());
                statement.setString(4, item.prefectureCode());
                statement.setString(5, item.areaName());
                statement.setString(6, item.areaCode());
                statement.setString(7, item.cityName());
                statement.setString(8, item.cityCode());
                statement.setString(9, item.intensity());
                setDouble(statement, 10, item.intensityNumeric());
                statement.setString(11, item.longPeriodClass());
                statement.setBoolean(12, item.maximumFlag());
                statement.setString(13, item.reviseType());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static Map<String, Long> insertStationIntensities(Connection connection, long eventId, EarthquakeEventEnvelope envelope) throws SQLException {
        Map<String, Long> stationIdByKey = new HashMap<>();
        String sql = """
            INSERT INTO jma_earthquake_station_intensity (
                earthquake_event_id, station_type, source_type, prefecture_name, prefecture_code,
                area_name, area_code, city_name, city_code, station_name, station_code,
                intensity, observation_status, realtime_intensity_text, realtime_intensity_value,
                intensity_numeric, long_period_class, revise_type, is_external
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        for (EarthquakeStationIntensity station : envelope.stationIntensities()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                statement.setLong(index++, eventId);
                statement.setString(index++, station.stationType());
                statement.setString(index++, station.sourceType());
                statement.setString(index++, station.prefectureName());
                statement.setString(index++, station.prefectureCode());
                statement.setString(index++, station.areaName());
                statement.setString(index++, station.areaCode());
                statement.setString(index++, station.cityName());
                statement.setString(index++, station.cityCode());
                statement.setString(index++, station.stationName());
                statement.setString(index++, station.stationCode());
                statement.setString(index++, station.intensity());
                statement.setString(index++, station.observationStatus());
                statement.setString(index++, station.realtimeIntensityText());
                setDouble(statement, index++, station.realtimeIntensityValue());
                setDouble(statement, index++, station.intensityNumeric());
                statement.setString(index++, station.longPeriodClass());
                statement.setString(index++, station.reviseType());
                statement.setBoolean(index, station.external());

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    stationIdByKey.put(stationKey(station), rs.getLong(1));
                }
            }
        }
        return stationIdByKey;
    }

    private static void insertLongPeriodMetrics(
        Connection connection,
        Map<String, Long> stationIdByKey,
        EarthquakeEventEnvelope envelope
    ) throws SQLException {
        String sql = """
            INSERT INTO jma_long_period_station_metric (
                station_intensity_id, metric_kind, periodic_band, period_unit,
                value_text, value_numeric, unit
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (LongPeriodStationMetric metric : envelope.longPeriodMetrics()) {
                Long stationIntensityId = findStationIntensityIdForMetric(stationIdByKey, metric.stationCode());
                if (stationIntensityId == null) {
                    continue;
                }
                statement.setLong(1, stationIntensityId);
                statement.setString(2, metric.metricKind());
                statement.setString(3, metric.periodicBand());
                statement.setString(4, metric.periodUnit());
                statement.setString(5, metric.valueText());
                setDouble(statement, 6, metric.valueNumeric());
                statement.setString(7, metric.unit());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertEewForecastAreas(Connection connection, long eventId, EarthquakeEventEnvelope envelope) throws SQLException {
        String sql = """
            INSERT INTO jma_eew_forecast_area (
                earthquake_event_id, info_scope, prefecture_name, prefecture_code, kind_name, kind_code,
                last_kind_name, last_kind_code, area_name, area_code, category_kind_name, category_kind_code,
                forecast_int_from, forecast_int_to, forecast_lg_int_from, forecast_lg_int_to,
                forecast_max_intensity, forecast_max_long_period_class, condition_text, arrival_time
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (EewForecastArea area : envelope.eewForecastAreas()) {
                int index = 1;
                statement.setLong(index++, eventId);
                statement.setString(index++, area.infoScope());
                statement.setString(index++, area.prefectureName());
                statement.setString(index++, area.prefectureCode());
                statement.setString(index++, area.kindName());
                statement.setString(index++, area.kindCode());
                statement.setString(index++, area.lastKindName());
                statement.setString(index++, area.lastKindCode());
                statement.setString(index++, area.areaName());
                statement.setString(index++, area.areaCode());
                statement.setString(index++, area.categoryKindName());
                statement.setString(index++, area.categoryKindCode());
                statement.setString(index++, area.forecastIntFrom());
                statement.setString(index++, area.forecastIntTo());
                statement.setString(index++, area.forecastLgIntFrom());
                statement.setString(index++, area.forecastLgIntTo());
                statement.setString(index++, area.forecastMaxIntensity());
                statement.setString(index++, area.forecastMaxLongPeriodClass());
                statement.setString(index++, area.conditionText());
                setOffsetDateTime(statement, index, area.arrivalTime());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertSpecialTextBlocks(Connection connection, long eventId, EarthquakeEventEnvelope envelope) throws SQLException {
        String sql = """
            INSERT INTO jma_earthquake_special_text_block (
                earthquake_event_id, block_type, block_title, sort_order, text_value
            ) VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (EarthquakeSpecialTextBlock block : envelope.specialTextBlocks()) {
                statement.setLong(1, eventId);
                statement.setString(2, block.blockType());
                statement.setString(3, block.blockTitle());
                setInteger(statement, 4, block.sortOrder());
                statement.setString(5, block.textValue());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertNoticeItems(Connection connection, long eventId, EarthquakeEventEnvelope envelope) throws SQLException {
        String sql = """
            INSERT INTO jma_earthquake_notice_item (
                earthquake_event_id, notice_kind, notice_title, notice_text, area_name, area_code, sort_order
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (EarthquakeNoticeItem item : envelope.noticeItems()) {
                statement.setLong(1, eventId);
                statement.setString(2, item.noticeKind());
                statement.setString(3, item.noticeTitle());
                statement.setString(4, item.noticeText());
                statement.setString(5, item.areaName());
                statement.setString(6, item.areaCode());
                setInteger(statement, 7, item.sortOrder());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertCountItems(Connection connection, long eventId, EarthquakeEventEnvelope envelope) throws SQLException {
        String sql = """
            INSERT INTO jma_earthquake_count_item (
                earthquake_event_id, item_type, start_time, end_time,
                number_of_events, number_of_felt_events, sort_order
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (EarthquakeCountItem item : envelope.countItems()) {
                statement.setLong(1, eventId);
                statement.setString(2, item.itemType());
                setOffsetDateTime(statement, 3, item.startTime());
                setOffsetDateTime(statement, 4, item.endTime());
                setInteger(statement, 5, item.numberOfEvents());
                setInteger(statement, 6, item.numberOfFeltEvents());
                setInteger(statement, 7, item.sortOrder());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void deleteByEventId(Connection connection, String tableName, long eventId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "DELETE FROM " + tableName + " WHERE earthquake_event_id = ?"
        )) {
            statement.setLong(1, eventId);
            statement.executeUpdate();
        }
    }

    private static void deleteSingleton(Connection connection, String tableName, long eventId) throws SQLException {
        deleteByEventId(connection, tableName, eventId);
    }

    private static Long findStationIntensityIdForMetric(Map<String, Long> stationIdByKey, String stationCode) {
        for (Map.Entry<String, Long> entry : stationIdByKey.entrySet()) {
            if (entry.getKey().endsWith("::" + stationCode)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String stationKey(EarthquakeStationIntensity station) {
        return station.stationType() + "::" + station.stationCode();
    }

    private static void setOffsetDateTime(PreparedStatement statement, int index, OffsetDateTime value) throws SQLException {
        if (value == null) {
            statement.setTimestamp(index, null);
        } else {
            statement.setTimestamp(index, Timestamp.from(value.toInstant()));
        }
    }

    private static void setInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setObject(index, null);
        } else {
            statement.setInt(index, value);
        }
    }

    private static void setDouble(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setObject(index, null);
        } else {
            statement.setDouble(index, value);
        }
    }
}
