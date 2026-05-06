package com.twins.crawler.dtos;

public record EarthquakeHypocenter(
    String areaName,
    String areaCode,
    String reduceName,
    String reduceCode,
    String landOrSea,
    String coordinateRaw,
    Double latitude,
    Double longitude,
    Integer depthM,
    String depthCondition,
    String epicenterAccuracyRank,
    String epicenterAccuracyRank2,
    String depthAccuracyRank,
    String magnitudeCalculationRank,
    Integer numberOfMagnitudeCalculation,
    Double magnitude,
    String magnitudeType,
    String textualDescription
) {
}
