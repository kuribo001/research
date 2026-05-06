package com.twins.crawler.dtos;

public record LongPeriodStationMetric(
    String stationCode,
    String metricKind,
    String periodicBand,
    String periodUnit,
    String valueText,
    Double valueNumeric,
    String unit
) {
}
