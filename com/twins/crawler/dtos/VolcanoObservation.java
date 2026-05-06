package com.twins.crawler.dtos;

public record VolcanoObservation(
    ColorPlume colorPlume,
    WindAboveCrater windAboveCrater,
    String otherObservation
) {
}
