package com.twins.crawler.dtos;

public record EarthquakeIntensityArea(
    String sourceType,
    String areaName,
    String areaCode,
    String intensity,
    Double intensityNumeric,
    String longPeriodClass,
    Integer sortOrder
) {
}
