package com.twins.crawler.dtos;

public record VolcanoInfoContent(
    String headline,
    String activity,
    String prevention,
    String nextAdvisory,
    String appendix
) {
}
