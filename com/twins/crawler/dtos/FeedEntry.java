package com.twins.crawler.dtos;

import java.time.OffsetDateTime;

public record FeedEntry(
    String entryId, // <entry><id>
    String title, // <entry><title>
    String content, // <entry><content>
    String authorName, // <entry><author><name>
    String detailUrl, // <entry><link @href>
    String messageCode, // Parsed from detailUrl (VFVO53, VXSE53...)
    OffsetDateTime updatedAt // <entry><updated>
) {
}
