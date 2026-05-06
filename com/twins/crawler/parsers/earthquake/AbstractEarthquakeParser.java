package com.twins.crawler.parsers.earthquake;

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
import com.twins.crawler.dtos.ReportHead;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class AbstractEarthquakeParser implements EarthquakeParser {
    @Override
    public final EarthquakeEventEnvelope parse(String messageCode, String xml) {
        Objects.requireNonNull(messageCode, "messageCode must not be null");
        Objects.requireNonNull(xml, "xml must not be null");
        if (!supports(messageCode)) {
            throw new EarthquakeParserException("Parser " + getClass().getSimpleName()
                + " does not support message code " + messageCode);
        }
        Document document = JmaXml.parse(xml);
        return parseDocument(messageCode, document);
    }

    protected abstract EarthquakeEventEnvelope parseDocument(String messageCode, Document document);

    protected EarthquakeEvent parseCommonEvent(String messageCode, Document document) {
        Element head = JmaXml.requiredDirectChild(document.getDocumentElement(), "Head");
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element earthquake = JmaXml.firstDescendant(body, "Earthquake");

        ReportHead reportHead = new ReportHead(
            JmaXml.directChildText(head, "InfoKind"),
            JmaXml.directChildText(head, "InfoType"),
            JmaXml.directChildText(head, "EventID"),
            JmaXml.parseOffsetDateTime(JmaXml.directChildText(head, "ReportDateTime"))
        );

        OffsetDateTime targetDateTime = JmaXml.parseOffsetDateTime(JmaXml.directChildText(head, "TargetDateTime"));
        OffsetDateTime eventAt = earthquake == null ? null : JmaXml.parseOffsetDateTime(JmaXml.directChildText(earthquake, "OriginTime"));
        OffsetDateTime eventAtUtc = eventAt == null ? null : eventAt.withOffsetSameInstant(ZoneOffset.UTC);
        String eventTimePrecision = eventAt == null ? null : "second";
        String headlineText = JmaXml.directChildText(JmaXml.directChild(head, "Headline"), "Text");
        Double magnitude = JmaXml.parseDouble(JmaXml.directChildText(JmaXml.firstDescendant(earthquake, "Magnitude"), null));
        String magnitudeType = JmaXml.attributeValue(JmaXml.firstDescendant(earthquake, "Magnitude"), "type");
        String maxIntensity = JmaXml.directChildText(JmaXml.firstDescendant(body, "Intensity"), "MaxInt");
        String domesticTsunami = JmaXml.directChildText(JmaXml.firstDescendant(body, "DomesticTsunami"), null);
        String foreignTsunami = JmaXml.directChildText(JmaXml.firstDescendant(body, "ForeignTsunami"), null);

        return new EarthquakeEvent(
            reportHead,
            messageCode,
            family().name(),
            JmaXml.directChildText(head, "Title"),
            JmaXml.parseInteger(JmaXml.directChildText(head, "Serial")),
            targetDateTime,
            eventAt,
            eventAtUtc,
            eventTimePrecision,
            headlineText,
            JmaXml.directChildText(head, "NextAdvisory"),
            magnitude,
            magnitudeType,
            maxIntensity,
            domesticTsunami,
            foreignTsunami,
            "取消".equals(reportHead.infoType())
        );
    }

    protected EarthquakeHypocenter parseHypocenter(Document document) {
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element hypocenter = JmaXml.firstDescendant(body, "Hypocenter");
        if (hypocenter == null) {
            return null;
        }
        Element areaNode = JmaXml.firstDescendant(hypocenter, "Area");
        Element coordinate = JmaXml.firstDescendant(areaNode, "Coordinate");
        Element accuracy = JmaXml.directChild(hypocenter, "Accuracy");
        Element epicenter = JmaXml.directChild(accuracy, "Epicenter");
        Element depth = JmaXml.directChild(accuracy, "Depth");
        Element magnitudeCalculation = JmaXml.directChild(accuracy, "MagnitudeCalculation");

        Double latitude = JmaXml.parseCoordinateLatitude(coordinate == null ? null : coordinate.getTextContent());
        Double longitude = JmaXml.parseCoordinateLongitude(coordinate == null ? null : coordinate.getTextContent());
        Integer depthM = JmaXml.parseCoordinateDepthMeters(coordinate == null ? null : coordinate.getTextContent());

        return new EarthquakeHypocenter(
            JmaXml.directChildText(areaNode, "Name"),
            JmaXml.directChildText(JmaXml.firstDescendant(areaNode, "Code"), null),
            JmaXml.directChildText(areaNode, "ReduceName") != null
                ? JmaXml.directChildText(areaNode, "ReduceName")
                : JmaXml.directChildText(areaNode, "DetailedName"),
            JmaXml.directChildText(JmaXml.firstDescendant(areaNode, "ReduceCode"), null) != null
                ? JmaXml.directChildText(JmaXml.firstDescendant(areaNode, "ReduceCode"), null)
                : JmaXml.directChildText(JmaXml.firstDescendant(areaNode, "DetailedCode"), null),
            JmaXml.directChildText(areaNode, "LandOrSea"),
            coordinate == null ? null : coordinate.getTextContent(),
            latitude,
            longitude,
            depthM,
            null,
            JmaXml.attributeValue(epicenter, "rank"),
            JmaXml.attributeValue(epicenter, "rank2"),
            JmaXml.attributeValue(depth, "rank"),
            JmaXml.attributeValue(magnitudeCalculation, "rank"),
            JmaXml.parseInteger(JmaXml.directChildText(accuracy, "NumberOfMagnitudeCalculation")),
            JmaXml.parseDouble(JmaXml.directChildText(JmaXml.firstDescendant(body, "Magnitude"), null)),
            JmaXml.attributeValue(JmaXml.firstDescendant(body, "Magnitude"), "type"),
            JmaXml.attributeValue(coordinate, "description")
        );
    }

    protected EarthquakeComment parseComment(Document document) {
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element comments = JmaXml.firstDescendant(body, "Comments");
        if (comments == null) {
            return null;
        }

        Element forecastComment = JmaXml.firstDescendant(comments, "ForecastComment");
        Element varComment = JmaXml.firstDescendant(comments, "VarComment");
        Element warningComment = JmaXml.firstDescendant(comments, "WarningComment");
        String forecastText = JmaXml.directChildText(forecastComment, "Text");
        String varCommentText = JmaXml.directChildText(varComment, "Text");
        String warningText = JmaXml.directChildText(warningComment, "Text");
        String warningCode = JmaXml.directChildText(warningComment, "Code");
        String uri = JmaXml.directChildText(comments, "URI");

        return new EarthquakeComment(
            null,
            JmaXml.directChildText(comments, "FreeFormComment"),
            joinText(varCommentText, uri),
            forecastText != null && forecastText.contains("津波") ? forecastText : null,
            forecastText,
            null,
            warningText,
            warningCode
        );
    }

    protected EarthquakeComment parseNarrativeComment(Document document) {
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element earthquakeInfo = JmaXml.firstDescendant(body, "EarthquakeInfo");
        EarthquakeComment base = parseComment(document);
        String bodyText = firstNonBlank(
            JmaXml.directChildText(body, "Text"),
            JmaXml.directChildText(earthquakeInfo, "Text")
        );
        String additionalText = joinText(
            JmaXml.directChildText(body, "NextAdvisory"),
            base == null ? null : base.additionalText()
        );
        String appendix = firstNonBlank(
            JmaXml.directChildText(earthquakeInfo, "Appendix"),
            base == null ? null : base.appendix()
        );

        if (base == null && bodyText == null && additionalText == null && appendix == null) {
            return null;
        }

        if (base == null) {
            return new EarthquakeComment(
                bodyText,
                null,
                additionalText,
                null,
                null,
                appendix,
                null,
                null
            );
        }

        return new EarthquakeComment(
            bodyText,
            base.freeText(),
            additionalText,
            base.tsunamiComment(),
            base.forecastComment(),
            appendix,
            base.warningCommentText(),
            base.warningCommentCode()
        );
    }

    protected EarthquakeEvent withNextAdvisory(EarthquakeEvent event, String nextAdvisory) {
        if (nextAdvisory == null) {
            return event;
        }
        return new EarthquakeEvent(
            event.head(),
            event.messageCode(),
            event.familyName(),
            event.title(),
            event.serial(),
            event.targetDateTime(),
            event.eventAt(),
            event.eventAtUtc(),
            event.eventTimePrecision(),
            event.headlineText(),
            nextAdvisory,
            event.magnitude(),
            event.magnitudeType(),
            event.maxIntensity(),
            event.domesticTsunami(),
            event.foreignTsunami(),
            event.cancelled()
        );
    }

    protected List<EarthquakeCountItem> parseCountItems(Document document) {
        List<EarthquakeCountItem> items = new ArrayList<>();
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element earthquakeCount = JmaXml.firstDescendant(body, "EarthquakeCount");
        if (earthquakeCount == null) {
            return items;
        }

        int sortOrder = 1;
        for (Element item : JmaXml.directChildren(earthquakeCount, "Item")) {
            items.add(new EarthquakeCountItem(
                JmaXml.attributeValue(item, "type"),
                JmaXml.parseOffsetDateTime(JmaXml.directChildText(item, "StartTime")),
                JmaXml.parseOffsetDateTime(JmaXml.directChildText(item, "EndTime")),
                JmaXml.parseInteger(JmaXml.directChildText(item, "Number")),
                JmaXml.parseInteger(JmaXml.directChildText(item, "FeltNumber")),
                sortOrder++
            ));
        }
        return items;
    }

    protected EarthquakeSpecialInformation parseSpecialInformation(Document document) {
        Element head = JmaXml.requiredDirectChild(document.getDocumentElement(), "Head");
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element earthquakeInfo = JmaXml.firstDescendant(body, "EarthquakeInfo");
        if (earthquakeInfo == null) {
            return null;
        }

        Element infoSerial = JmaXml.directChild(earthquakeInfo, "InfoSerial");
        return new EarthquakeSpecialInformation(
            JmaXml.directChildText(earthquakeInfo, "InfoKind"),
            null,
            null,
            JmaXml.directChildText(infoSerial, "Name"),
            JmaXml.directChildText(infoSerial, "Code"),
            JmaXml.directChildText(head, "InfoType"),
            JmaXml.attributeValue(earthquakeInfo, "type"),
            JmaXml.directChildText(body, "NextAdvisory")
        );
    }

    protected List<EarthquakeSpecialTextBlock> parseSpecialTextBlocks(Document document) {
        List<EarthquakeSpecialTextBlock> blocks = new ArrayList<>();
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element earthquakeInfo = JmaXml.firstDescendant(body, "EarthquakeInfo");

        int sortOrder = 1;
        if (earthquakeInfo != null) {
            String text = JmaXml.directChildText(earthquakeInfo, "Text");
            if (text != null) {
                blocks.add(new EarthquakeSpecialTextBlock(
                    "main_text",
                    JmaXml.directChildText(earthquakeInfo, "InfoKind"),
                    sortOrder++,
                    text
                ));
            }

            String appendix = JmaXml.directChildText(earthquakeInfo, "Appendix");
            if (appendix != null) {
                blocks.add(new EarthquakeSpecialTextBlock(
                    "appendix",
                    "Appendix",
                    sortOrder++,
                    appendix
                ));
            }
        }

        String nextAdvisory = JmaXml.directChildText(body, "NextAdvisory");
        if (nextAdvisory != null) {
            blocks.add(new EarthquakeSpecialTextBlock(
                "next_advisory",
                "NextAdvisory",
                sortOrder,
                nextAdvisory
            ));
        }
        return blocks;
    }

    protected List<EarthquakeNoticeItem> parseNoticeItems(Document document) {
        List<EarthquakeNoticeItem> items = new ArrayList<>();
        Element report = document.getDocumentElement();
        Element head = JmaXml.requiredDirectChild(report, "Head");
        Element body = JmaXml.requiredDirectChild(report, "Body");

        String noticeText = JmaXml.directChildText(body, "Text");
        if (noticeText != null) {
            items.add(new EarthquakeNoticeItem(
                JmaXml.directChildText(head, "InfoKind"),
                JmaXml.directChildText(head, "Title"),
                noticeText,
                null,
                null,
                1
            ));
        }
        return items;
    }

    protected List<EewForecastArea> parseEewForecastAreas(Document document) {
        List<EewForecastArea> areas = new ArrayList<>();
        Element report = document.getDocumentElement();
        Element head = JmaXml.requiredDirectChild(report, "Head");
        Element body = JmaXml.requiredDirectChild(report, "Body");

        Element headline = JmaXml.directChild(head, "Headline");
        if (headline != null) {
            for (Element information : JmaXml.directChildren(headline, "Information")) {
                String infoScope = JmaXml.attributeValue(information, "type");
                for (Element item : JmaXml.directChildren(information, "Item")) {
                    Element kind = JmaXml.directChild(item, "Kind");
                    Element lastKind = JmaXml.directChild(item, "LastKind");
                    Element areaContainer = JmaXml.directChild(item, "Areas");
                    for (Element area : JmaXml.directChildren(areaContainer, "Area")) {
                        areas.add(new EewForecastArea(
                            infoScope,
                            null,
                            null,
                            JmaXml.directChildText(kind, "Name"),
                            JmaXml.directChildText(kind, "Code"),
                            JmaXml.directChildText(lastKind, "Name"),
                            JmaXml.directChildText(lastKind, "Code"),
                            JmaXml.directChildText(area, "Name"),
                            JmaXml.directChildText(area, "Code"),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        ));
                    }
                }
            }
        }

        Element forecast = JmaXml.firstDescendant(body, "Forecast");
        if (forecast == null) {
            return areas;
        }

        for (Element prefecture : JmaXml.directChildren(forecast, "Pref")) {
            String prefectureName = JmaXml.directChildText(prefecture, "Name");
            String prefectureCode = JmaXml.directChildText(prefecture, "Code");
            Element globalForecastInt = JmaXml.directChild(forecast, "ForecastInt");
            Element globalForecastLgInt = JmaXml.directChild(forecast, "ForecastLgInt");
            String forecastMaxIntensity = rangeText(globalForecastInt);
            String forecastMaxLongPeriodClass = rangeText(globalForecastLgInt);
            for (Element area : JmaXml.directChildren(prefecture, "Area")) {
                Element category = JmaXml.directChild(area, "Category");
                Element kind = JmaXml.directChild(category, "Kind");
                Element forecastInt = JmaXml.directChild(area, "ForecastInt");
                Element forecastLgInt = JmaXml.directChild(area, "ForecastLgInt");

                areas.add(new EewForecastArea(
                    "forecast_area",
                    prefectureName,
                    prefectureCode,
                    null,
                    null,
                    null,
                    null,
                    JmaXml.directChildText(area, "Name"),
                    JmaXml.directChildText(area, "Code"),
                    JmaXml.directChildText(kind, "Name"),
                    JmaXml.directChildText(kind, "Code"),
                    JmaXml.directChildText(forecastInt, "From"),
                    JmaXml.directChildText(forecastInt, "To"),
                    JmaXml.directChildText(forecastLgInt, "From"),
                    JmaXml.directChildText(forecastLgInt, "To"),
                    forecastMaxIntensity,
                    forecastMaxLongPeriodClass,
                    JmaXml.directChildText(area, "Condition"),
                    JmaXml.parseOffsetDateTime(JmaXml.directChildText(area, "ArrivalTime"))
                ));
            }
        }
        return areas;
    }

    protected EewDetail parseEewDetail(Document document) {
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element forecast = JmaXml.firstDescendant(body, "Forecast");
        Element earthquake = JmaXml.firstDescendant(body, "Earthquake");
        Element appendix = JmaXml.firstDescendant(forecast, "Appendix");
        Element comments = JmaXml.firstDescendant(body, "Comments");
        Element warningComment = JmaXml.firstDescendant(comments, "WarningComment");

        if (forecast == null && earthquake == null && warningComment == null) {
            return null;
        }

        return new EewDetail(
            JmaXml.parseInteger(JmaXml.directChildText(JmaXml.requiredDirectChild(document.getDocumentElement(), "Head"), "Serial")),
            false,
            false,
            JmaXml.parseOffsetDateTime(JmaXml.directChildText(earthquake, "OriginTime")),
            JmaXml.parseOffsetDateTime(JmaXml.directChildText(earthquake, "ArrivalTime")),
            JmaXml.directChildText(appendix, "MaxIntChange"),
            JmaXml.directChildText(appendix, "MaxLgIntChange"),
            JmaXml.directChildText(appendix, "MaxIntChangeReason"),
            JmaXml.directChildText(warningComment, "Text"),
            JmaXml.directChildText(warningComment, "Code"),
            JmaXml.directChildText(body, "Text")
        );
    }

    protected void collectHeadlineAreas(
        Document document,
        List<EarthquakeIntensityArea> intensityAreas,
        List<EarthquakeMunicipalityIntensity> municipalityIntensities,
        String areaSourceType,
        String citySourceType
    ) {
        Element head = JmaXml.requiredDirectChild(document.getDocumentElement(), "Head");
        Element headline = JmaXml.directChild(head, "Headline");
        if (headline == null) {
            return;
        }

        for (Element information : JmaXml.directChildren(headline, "Information")) {
            String type = JmaXml.attributeValue(information, "type");
            for (Element item : JmaXml.directChildren(information, "Item")) {
                String intensity = JmaXml.directChildText(JmaXml.directChild(item, "Kind"), "Name");
                Element areas = JmaXml.directChild(item, "Areas");
                for (Element area : JmaXml.directChildren(areas, "Area")) {
                    if (type != null && type.contains("市町村等")) {
                        municipalityIntensities.add(new EarthquakeMunicipalityIntensity(
                            citySourceType,
                            null,
                            null,
                            null,
                            null,
                            JmaXml.directChildText(area, "Name"),
                            JmaXml.directChildText(area, "Code"),
                            intensity,
                            null,
                            null,
                            false,
                            null
                        ));
                    } else {
                        intensityAreas.add(new EarthquakeIntensityArea(
                            areaSourceType,
                            JmaXml.directChildText(area, "Name"),
                            JmaXml.directChildText(area, "Code"),
                            intensity,
                            null,
                            null,
                            intensityAreas.size() + 1
                        ));
                    }
                }
            }
        }
    }

    protected void collectAreaOnlyObservations(
        Document document,
        List<EarthquakeIntensityArea> intensityAreas,
        String sourceType
    ) {
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element observation = JmaXml.firstDescendant(body, "Observation");
        if (observation == null) {
            return;
        }

        for (Element prefecture : JmaXml.directChildren(observation, "Pref")) {
            for (Element area : JmaXml.directChildren(prefecture, "Area")) {
                intensityAreas.add(new EarthquakeIntensityArea(
                    sourceType,
                    JmaXml.directChildText(area, "Name"),
                    JmaXml.directChildText(area, "Code"),
                    JmaXml.directChildText(area, "MaxInt"),
                    null,
                    JmaXml.directChildText(area, "MaxLgInt"),
                    intensityAreas.size() + 1
                ));
            }
        }
    }

    protected void collectDetailedObservations(
        Document document,
        List<EarthquakeIntensityArea> intensityAreas,
        List<EarthquakeMunicipalityIntensity> municipalityIntensities,
        List<EarthquakeStationIntensity> stationIntensities,
        String areaSourceType,
        String citySourceType,
        String stationSourceType,
        String stationType,
        boolean includeStations
    ) {
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        Element observation = JmaXml.firstDescendant(body, "Observation");
        if (observation == null) {
            return;
        }

        for (Element prefecture : JmaXml.directChildren(observation, "Pref")) {
            String prefectureName = JmaXml.directChildText(prefecture, "Name");
            String prefectureCode = JmaXml.directChildText(prefecture, "Code");

            for (Element area : JmaXml.directChildren(prefecture, "Area")) {
                String areaName = JmaXml.directChildText(area, "Name");
                String areaCode = JmaXml.directChildText(area, "Code");

                intensityAreas.add(new EarthquakeIntensityArea(
                    areaSourceType,
                    areaName,
                    areaCode,
                    JmaXml.directChildText(area, "MaxInt"),
                    null,
                    JmaXml.directChildText(area, "MaxLgInt"),
                    intensityAreas.size() + 1
                ));

                for (Element city : JmaXml.directChildren(area, "City")) {
                    municipalityIntensities.add(new EarthquakeMunicipalityIntensity(
                        citySourceType,
                        prefectureName,
                        prefectureCode,
                        areaName,
                        areaCode,
                        JmaXml.directChildText(city, "Name"),
                        JmaXml.directChildText(city, "Code"),
                        JmaXml.directChildText(city, "MaxInt"),
                        null,
                        JmaXml.directChildText(city, "MaxLgInt"),
                        false,
                        JmaXml.directChildText(city, "Revise")
                    ));

                    if (!includeStations) {
                        continue;
                    }

                    for (Element station : JmaXml.directChildren(city, "IntensityStation")) {
                        stationIntensities.add(new EarthquakeStationIntensity(
                            stationType,
                            stationSourceType,
                            prefectureName,
                            prefectureCode,
                            areaName,
                            areaCode,
                            JmaXml.directChildText(city, "Name"),
                            JmaXml.directChildText(city, "Code"),
                            JmaXml.directChildText(station, "Name"),
                            JmaXml.directChildText(station, "Code"),
                            JmaXml.directChildText(station, "Int"),
                            null,
                            null,
                            null,
                            null,
                            JmaXml.directChildText(station, "LgInt"),
                            JmaXml.directChildText(station, "Revise"),
                            false
                        ));
                    }
                }
            }
        }
    }

    protected Set<String> singletonCode(String messageCode) {
        return Set.of(messageCode);
    }

    private String joinText(String... values) {
        List<String> parts = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                parts.add(value.trim());
            }
        }
        return parts.isEmpty() ? null : String.join("\n", parts);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String rangeText(Element rangeElement) {
        if (rangeElement == null) {
            return null;
        }
        String from = JmaXml.directChildText(rangeElement, "From");
        String to = JmaXml.directChildText(rangeElement, "To");
        if (from == null && to == null) {
            return null;
        }
        if (from == null) {
            return to;
        }
        if (to == null || from.equals(to)) {
            return from;
        }
        return from + "-" + to;
    }
}
