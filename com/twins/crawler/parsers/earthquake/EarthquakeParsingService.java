package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import java.util.Objects;

public final class EarthquakeParsingService {
    private static final EarthquakeParserDispatcher DEFAULT_DISPATCHER =
        EarthquakeParserDispatcher.defaultDispatcher();

    private EarthquakeParsingService() {
    }

    public static EarthquakeEventEnvelope parse(String messageCode, String xml) {
        Objects.requireNonNull(messageCode, "messageCode must not be null");
        Objects.requireNonNull(xml, "xml must not be null");
        return DEFAULT_DISPATCHER.parse(messageCode, xml);
    }

    public static EarthquakeParserDispatcher defaultDispatcher() {
        return DEFAULT_DISPATCHER;
    }
}
