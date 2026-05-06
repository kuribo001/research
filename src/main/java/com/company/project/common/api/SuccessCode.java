package com.company.project.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    ORDER_CREATED("ORDER_CREATED"),
    BATCH_JOB_ACCEPTED("BATCH_JOB_ACCEPTED");

    private final String code;
}
