package com.twins.crawler.dtos;

import java.time.OffsetDateTime;

public record EewForecastArea(
    String infoScope,
    String prefectureName,
    String prefectureCode,
    String kindName,
    String kindCode,
    String lastKindName,
    String lastKindCode,
    String areaName,
    String areaCode,
    String categoryKindName,
    String categoryKindCode,
    String forecastIntFrom,
    String forecastIntTo,
    String forecastLgIntFrom,
    String forecastLgIntTo,
    String forecastMaxIntensity,
    String forecastMaxLongPeriodClass,
    String conditionText,
    OffsetDateTime arrivalTime
) {
}
