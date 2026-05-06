package com.twins.crawler.dtos;

import java.time.OffsetDateTime;
import java.util.List;

public record AshForecast(
    String type,
    OffsetDateTime startTime,
    OffsetDateTime endTime,
    List<AshForecastItem> items
) {
}
