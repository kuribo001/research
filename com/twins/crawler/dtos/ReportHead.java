package com.twins.crawler.dtos;

import java.time.OffsetDateTime;

public record ReportHead(
    String infoKind, // InfoKind
    String infoType, // InfoType
    String eventId, // EventID
    OffsetDateTime issuedAt // DateTime or ReportDateTime
) {
}
