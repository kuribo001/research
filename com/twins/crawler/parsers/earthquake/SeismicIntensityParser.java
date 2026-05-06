package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import com.twins.crawler.dtos.EarthquakeIntensityArea;
import com.twins.crawler.dtos.EarthquakeMunicipalityIntensity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;

public final class SeismicIntensityParser extends AbstractEarthquakeParser {
    private static final String MESSAGE_CODE = "VXSE51";

    @Override
    public EarthquakeMessageFamily family() {
        return EarthquakeMessageFamily.SEISMIC_INTENSITY;
    }

    @Override
    public Set<String> supportedMessageCodes() {
        return singletonCode(MESSAGE_CODE);
    }

    @Override
    protected EarthquakeEventEnvelope parseDocument(String messageCode, Document document) {
        List<EarthquakeIntensityArea> intensityAreas = new ArrayList<>();
        List<EarthquakeMunicipalityIntensity> municipalityIntensities = new ArrayList<>();

        collectHeadlineAreas(document, intensityAreas, municipalityIntensities, "headline_area", "headline_city");
        collectAreaOnlyObservations(document, intensityAreas, "observation_area");

        return new EarthquakeEventEnvelope(
            parseCommonEvent(messageCode, document),
            null,
            intensityAreas,
            municipalityIntensities,
            List.of(),
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
