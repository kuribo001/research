package com.twins.crawler.dtos;

import java.time.OffsetDateTime;

public record EarthquakeCountItem(
    String itemType,
    OffsetDateTime startTime,
    OffsetDateTime endTime,
    Integer numberOfEvents,
    Integer numberOfFeltEvents,
    Integer sortOrder
) {
}
