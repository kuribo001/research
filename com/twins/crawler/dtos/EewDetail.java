package com.twins.crawler.dtos;

import java.time.OffsetDateTime;

public record EewDetail(
    Integer reportNum,
    boolean lastReport,
    boolean plumAssumption,
    OffsetDateTime triggerOriginTime,
    OffsetDateTime triggerArrivalTime,
    String forecastMaxIntChange,
    String forecastMaxLgIntChange,
    String forecastMaxIntChangeReason,
    String warningText,
    String warningCode,
    String textualForecast
) {
}
