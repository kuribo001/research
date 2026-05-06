package com.twins.crawler.dtos;

public record FeedEntryRef(
    String entryId, // <entry><id>
    String detailUrl // <entry><link @href>
) {
}
