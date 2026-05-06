package com.twins.crawler.dtos;

public record ParsedCoordinate(
    double latitude,
    double longitude,
    Integer elevation
) {
}
