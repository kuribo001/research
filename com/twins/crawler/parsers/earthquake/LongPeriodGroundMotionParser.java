package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import com.twins.crawler.dtos.EarthquakeIntensityArea;
import com.twins.crawler.dtos.EarthquakeMunicipalityIntensity;
import com.twins.crawler.dtos.EarthquakeStationIntensity;
import com.twins.crawler.dtos.LongPeriodStationMetric;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class LongPeriodGroundMotionParser extends AbstractEarthquakeParser {
    private static final String MESSAGE_CODE = "VXSE62";

    @Override
    public EarthquakeMessageFamily family() {
        return EarthquakeMessageFamily.LONG_PERIOD_GROUND_MOTION;
    }

    @Override
    public Set<String> supportedMessageCodes() {
        return singletonCode(MESSAGE_CODE);
    }

    @Override
    protected EarthquakeEventEnvelope parseDocument(String messageCode, Document document) {
        List<EarthquakeIntensityArea> intensityAreas = new ArrayList<>();
        List<EarthquakeMunicipalityIntensity> municipalityIntensities = new ArrayList<>();
        List<EarthquakeStationIntensity> stationIntensities = new ArrayList<>();
        List<LongPeriodStationMetric> longPeriodMetrics = new ArrayList<>();

        collectHeadlineAreas(document, intensityAreas, municipalityIntensities, "headline_area", "headline_city");
        collectDetailedObservations(
            document,
            intensityAreas,
            municipalityIntensities,
            stationIntensities,
            "observation_area",
            "observation_city",
            "observation_station",
            "long_period_station",
            true
        );
        collectLongPeriodMetrics(document, longPeriodMetrics);

        return new EarthquakeEventEnvelope(
            parseCommonEvent(messageCode, document),
            parseHypocenter(document),
            intensityAreas,
            municipalityIntensities,
            stationIntensities,
            longPeriodMetrics,
            parseComment(document),
            List.of(),
            null,
            null,
            List.of(),
            List.of(),
            List.of()
        );
    }

    private void collectLongPeriodMetrics(Document document, List<LongPeriodStationMetric> metrics) {
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        for (Element station : JmaXml.descendants(body, "IntensityStation")) {
            String stationCode = JmaXml.directChildText(station, "Code");

            for (Element perPeriod : JmaXml.directChildren(station, "LgIntPerPeriod")) {
                metrics.add(new LongPeriodStationMetric(
                    stationCode,
                    "LgIntPerPeriod",
                    JmaXml.attributeValue(perPeriod, "PeriodicBand"),
                    JmaXml.attributeValue(perPeriod, "PeriodUnit"),
                    JmaXml.directChildText(perPeriod, null),
                    JmaXml.parseDouble(JmaXml.directChildText(perPeriod, null)),
                    null
                ));
            }

            Element sva = JmaXml.directChild(station, "Sva");
            if (sva != null) {
                metrics.add(new LongPeriodStationMetric(
                    stationCode,
                    "Sva",
                    null,
                    null,
                    JmaXml.directChildText(sva, null),
                    JmaXml.parseDouble(JmaXml.directChildText(sva, null)),
                    JmaXml.attributeValue(sva, "unit")
                ));
            }

            for (Element svaPerPeriod : JmaXml.directChildren(station, "SvaPerPeriod")) {
                metrics.add(new LongPeriodStationMetric(
                    stationCode,
                    "SvaPerPeriod",
                    JmaXml.attributeValue(svaPerPeriod, "PeriodicBand"),
                    JmaXml.attributeValue(svaPerPeriod, "PeriodUnit"),
                    JmaXml.directChildText(svaPerPeriod, null),
                    JmaXml.parseDouble(JmaXml.directChildText(svaPerPeriod, null)),
                    JmaXml.attributeValue(svaPerPeriod, "unit")
                ));
            }
        }
    }
}
