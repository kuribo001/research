package com.twins.crawler.dtos;

import java.time.OffsetDateTime;
import java.util.List;

public record WindAboveCrater(
    OffsetDateTime dateTime,
    List<WindLayer> elements
) {
}
