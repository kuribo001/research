package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import java.util.Set;

public interface EarthquakeParser {
    EarthquakeMessageFamily family();

    Set<String> supportedMessageCodes();

    EarthquakeEventEnvelope parse(String messageCode, String xml);

    default boolean supports(String messageCode) {
        return supportedMessageCodes().contains(messageCode);
    }
}
