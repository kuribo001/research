package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;

public final class SubsequentEarthquakeAdvisoryParser extends AbstractEarthquakeParser {
    private static final String MESSAGE_CODE = "VYSE60";

    @Override
    public EarthquakeMessageFamily family() {
        return EarthquakeMessageFamily.SUBSEQUENT_EARTHQUAKE_ADVISORY;
    }

    @Override
    public Set<String> supportedMessageCodes() {
        return singletonCode(MESSAGE_CODE);
    }

    @Override
    protected EarthquakeEventEnvelope parseDocument(String messageCode, Document document) {
        return new EarthquakeEventEnvelope(
            parseCommonEvent(messageCode, document),
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            parseNarrativeComment(document),
            List.of(),
            null,
            parseSpecialInformation(document),
            parseSpecialTextBlocks(document),
            List.of(),
            List.of()
        );
    }
}
