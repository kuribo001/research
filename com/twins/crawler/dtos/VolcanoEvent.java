package com.twins.crawler.dtos;

import java.time.OffsetDateTime;

public record VolcanoEvent(
    ReportHead head,
    OffsetDateTime eventAt,
    OffsetDateTime eventAtUtc,
    Volcano volcano,
    VolcanoActivity activity,
    VolcanoObservation observation,
    VolcanoInfoContent infoContent,
    Comments comments
) {
}
