package com.twins.crawler.dtos;

public record EarthquakeStationIntensity(
    String stationType,
    String sourceType,
    String prefectureName,
    String prefectureCode,
    String areaName,
    String areaCode,
    String cityName,
    String cityCode,
    String stationName,
    String stationCode,
    String intensity,
    String observationStatus,
    String realtimeIntensityText,
    Double realtimeIntensityValue,
    Double intensityNumeric,
    String longPeriodClass,
    String reviseType,
    boolean external
) {
}
