package com.twins.crawler.dtos;

import java.time.OffsetDateTime;

public record EarthquakeEvent(
    ReportHead head,
    String messageCode,
    String familyName,
    String title,
    Integer serial,
    OffsetDateTime targetDateTime,
    OffsetDateTime eventAt,
    OffsetDateTime eventAtUtc,
    String eventTimePrecision,
    String headlineText,
    String nextAdvisory,
    Double magnitude,
    String magnitudeType,
    String maxIntensity,
    String domesticTsunami,
    String foreignTsunami,
    boolean cancelled
) {
}
