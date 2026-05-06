package com.twins.crawler.dtos;

import java.util.List;

public record AshForecastItem(
    String kindName,
    List<AshArea> areas,
    String polygon // raw polygon string (WKT-like)
) {
}
