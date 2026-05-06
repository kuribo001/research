package com.twins.crawler.parsers.earthquake;

import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EarthquakeParserDispatcher {
    private final Map<String, EarthquakeParser> parsersByMessageCode;

    public EarthquakeParserDispatcher(List<EarthquakeParser> parsers) {
        Objects.requireNonNull(parsers, "parsers must not be null");
        Map<String, EarthquakeParser> parserMap = new HashMap<>();
        for (EarthquakeParser parser : parsers) {
            Objects.requireNonNull(parser, "parser must not be null");
            for (String messageCode : parser.supportedMessageCodes()) {
                EarthquakeParser previous = parserMap.putIfAbsent(messageCode, parser);
                if (previous != null) {
                    throw new EarthquakeParserException(
                        "Duplicate parser registration for message code " + messageCode
                            + ": " + previous.getClass().getSimpleName()
                            + " and " + parser.getClass().getSimpleName()
                    );
                }
            }
        }
        this.parsersByMessageCode = Collections.unmodifiableMap(parserMap);
    }

    public static EarthquakeParserDispatcher defaultDispatcher() {
        return new EarthquakeParserDispatcher(List.of(
            new EewParser(),
            new SeismicIntensityParser(),
            new HypocenterParser(),
            new HypocenterSeismicParser(),
            new EarthquakeActivityParser(),
            new EarthquakeCountParser(),
            new HypocenterUpdateParser(),
            new LongPeriodGroundMotionParser(),
            new NankaiParser(),
            new SubsequentEarthquakeAdvisoryParser(),
            new EarthquakeNoticeParser()
        ));
    }

    public EarthquakeParser parserFor(String messageCode) {
        Objects.requireNonNull(messageCode, "messageCode must not be null");
        EarthquakeParser parser = parsersByMessageCode.get(messageCode);
        if (parser == null) {
            throw new EarthquakeParserException("No parser registered for message code: " + messageCode);
        }
        return parser;
    }

    public EarthquakeEventEnvelope parse(String messageCode, String xml) {
        Objects.requireNonNull(xml, "xml must not be null");
        return parserFor(messageCode).parse(messageCode, xml);
    }
}
