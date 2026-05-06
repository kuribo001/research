package com.twins.crawler.dtos;

import java.util.List;

public record EarthquakeEventEnvelope(
    EarthquakeEvent event,
    EarthquakeHypocenter hypocenter,
    List<EarthquakeIntensityArea> intensityAreas,
    List<EarthquakeMunicipalityIntensity> municipalityIntensities,
    List<EarthquakeStationIntensity> stationIntensities,
    List<LongPeriodStationMetric> longPeriodMetrics,
    EarthquakeComment comment,
    List<EewForecastArea> eewForecastAreas,
    EewDetail eewDetail,
    EarthquakeSpecialInformation specialInformation,
    List<EarthquakeSpecialTextBlock> specialTextBlocks,
    List<EarthquakeNoticeItem> noticeItems,
    List<EarthquakeCountItem> countItems
) {
}
