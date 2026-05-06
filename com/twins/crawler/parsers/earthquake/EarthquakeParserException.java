package com.twins.crawler.parsers.earthquake;

public class EarthquakeParserException extends RuntimeException {
    public EarthquakeParserException(String message) {
        super(message);
    }

    public EarthquakeParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
