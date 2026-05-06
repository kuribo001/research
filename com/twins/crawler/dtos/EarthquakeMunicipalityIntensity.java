package com.twins.crawler.dtos;

public record EarthquakeMunicipalityIntensity(
    String sourceType,
    String prefectureName,
    String prefectureCode,
    String areaName,
    String areaCode,
    String cityName,
    String cityCode,
    String intensity,
    Double intensityNumeric,
    String longPeriodClass,
    boolean maximumFlag,
    String reviseType
) {
}
