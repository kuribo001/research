package com.twins.crawler.dtos;

public record WindLayer(
    Integer altitudeM,
    String direction,
    Integer speedMs
) {
}
