package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;

public final class HypocenterParser extends AbstractEarthquakeParser {
    private static final String MESSAGE_CODE = "VXSE52";

    @Override
    public EarthquakeMessageFamily family() {
        return EarthquakeMessageFamily.HYPOCENTER;
    }

    @Override
    public Set<String> supportedMessageCodes() {
        return singletonCode(MESSAGE_CODE);
    }

    @Override
    protected EarthquakeEventEnvelope parseDocument(String messageCode, Document document) {
        return new EarthquakeEventEnvelope(
            parseCommonEvent(messageCode, document),
            parseHypocenter(document),
            List.of(),
            List.of(),
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
