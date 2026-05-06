package com.twins.crawler.dtos;

public record EarthquakeNoticeItem(
    String noticeKind,
    String noticeTitle,
    String noticeText,
    String areaName,
    String areaCode,
    Integer sortOrder
) {
}
