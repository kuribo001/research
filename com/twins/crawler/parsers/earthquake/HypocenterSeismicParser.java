package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import com.twins.crawler.dtos.EarthquakeIntensityArea;
import com.twins.crawler.dtos.EarthquakeMunicipalityIntensity;
import com.twins.crawler.dtos.EarthquakeStationIntensity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class HypocenterSeismicParser extends AbstractEarthquakeParser {
    private static final String MESSAGE_CODE = "VXSE53";

    @Override
    public EarthquakeMessageFamily family() {
        return EarthquakeMessageFamily.HYPOCENTER_SEISMIC;
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

        collectHeadlineAreas(document, intensityAreas, municipalityIntensities, "headline_area", "headline_city");
        collectDetailedObservations(
            document,
            intensityAreas,
            municipalityIntensities,
            stationIntensities,
            "observation_area",
            "observation_city",
            "observation_station",
            "seismic_intensity_station",
            true
        );

        return new EarthquakeEventEnvelope(
            parseCommonEvent(messageCode, document),
            parseHypocenter(document),
            intensityAreas,
            municipalityIntensities,
            stationIntensities,
            List.of(),
            parseComment(document),
            List.of(),
            null,
            null,
            List.of(),
            List.of(),
            List.of()
        );
    }
}
