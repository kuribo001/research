package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEvent;
import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class EewParser extends AbstractEarthquakeParser {
    private static final Set<String> MESSAGE_CODES = Set.of("VXSE42", "VXSE43", "VXSE44", "VXSE45");

    @Override
    public EarthquakeMessageFamily family() {
        return EarthquakeMessageFamily.EEW;
    }

    @Override
    public Set<String> supportedMessageCodes() {
        return MESSAGE_CODES;
    }

    @Override
    protected EarthquakeEventEnvelope parseDocument(String messageCode, Document document) {
        Element body = JmaXml.requiredDirectChild(document.getDocumentElement(), "Body");
        EarthquakeEvent baseEvent = parseCommonEvent(messageCode, document);
        EarthquakeEvent event = withNextAdvisory(baseEvent, JmaXml.directChildText(body, "NextAdvisory"));

        return new EarthquakeEventEnvelope(
            event,
            parseHypocenter(document),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            parseNarrativeComment(document),
            parseEewForecastAreas(document),
            parseEewDetail(document),
            null,
            List.of(),
            List.of(),
            List.of()
        );
    }
}
