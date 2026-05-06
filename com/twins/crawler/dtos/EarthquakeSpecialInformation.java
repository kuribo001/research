package com.twins.crawler.dtos;

public record EarthquakeSpecialInformation(
    String informationName,
    String informationKeyword,
    String informationCode,
    String serialName,
    String serialCode,
    String reportCondition,
    String advisoryType,
    String validityText
) {
}
