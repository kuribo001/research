package com.company.project.batch.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record BatchJobResponse(
    @Schema(example = "BATCH_JOB_ACCEPTED", description = "Stable success code for frontend i18n mapping")
    String code
) {
}
