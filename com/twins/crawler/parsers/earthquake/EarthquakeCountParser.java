package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEvent;
import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class EarthquakeCountParser extends AbstractEarthquakeParser {
    private static final String MESSAGE_CODE = "VXSE60";

    @Override
    public EarthquakeMessageFamily family() {
        return EarthquakeMessageFamily.EARTHQUAKE_COUNT;
    }

    @Override
    public Set<String> supportedMessageCodes() {
        return singletonCode(MESSAGE_CODE);
    }

    @Override
    protected EarthquakeEventEnvelope parseDocument(String messageCode, Document document) {
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        EarthquakeEvent event = withNextAdvisory(
            parseCommonEvent(messageCode, document),
            JmaXml.directChildText(body, "NextAdvisory")
        );

        return new EarthquakeEventEnvelope(
            event,
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            parseNarrativeComment(document),
            List.of(),
            null,
            null,
            List.of(),
            List.of(),
            parseCountItems(document)
        );
    }
}
