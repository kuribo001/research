package com.twins.crawler.dtos;

public record EarthquakeSpecialTextBlock(
    String blockType,
    String blockTitle,
    Integer sortOrder,
    String textValue
) {
}
