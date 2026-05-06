package com.twins.crawler.dtos;

public record Volcano(
    String code,
    String name,
    String coordinate, // raw coordinate string
    AlertKind currentKind,
    AlertKind lastKind,
    Crater crater
) {
}
