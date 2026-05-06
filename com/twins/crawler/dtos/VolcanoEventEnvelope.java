package com.twins.crawler.dtos;

import java.util.List;

public record VolcanoEventEnvelope(
    VolcanoEvent event,
    List<AshForecast> ashForecasts
) {
}
