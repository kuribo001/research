package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEvent;
import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class NankaiParser extends AbstractEarthquakeParser {
    private static final Set<String> MESSAGE_CODES = Set.of("VYSE50", "VYSE51", "VYSE52");

    @Override
    public EarthquakeMessageFamily family() {
        return EarthquakeMessageFamily.NANKAI;
    }

    @Override
    public Set<String> supportedMessageCodes() {
        return MESSAGE_CODES;
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
            parseSpecialInformation(document),
            parseSpecialTextBlocks(document),
            List.of(),
            List.of()
        );
    }
}
