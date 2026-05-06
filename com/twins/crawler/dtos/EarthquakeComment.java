package com.twins.crawler.dtos;

public record EarthquakeComment(
    String bodyText,
    String freeText,
    String additionalText,
    String tsunamiComment,
    String forecastComment,
    String appendix,
    String warningCommentText,
    String warningCommentCode
) {
}
