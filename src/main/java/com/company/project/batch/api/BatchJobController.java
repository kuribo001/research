package com.company.project.batch.api;

import com.company.project.batch.application.CustomerSyncJobFacade;
import com.company.project.batch.api.response.BatchJobResponse;
import com.company.project.common.api.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/batch-jobs")
@RequiredArgsConstructor
public class BatchJobController {

    private final CustomerSyncJobFacade customerSyncJobFacade;

    @PostMapping("/customer-sync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Trigger customer sync batch job")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Batch job accepted. Example code: BATCH_JOB_ACCEPTED")
    })
    public BatchJobResponse triggerCustomerSync() {
        customerSyncJobFacade.trigger();
        return new BatchJobResponse(SuccessCode.BATCH_JOB_ACCEPTED.getCode());
    }
}
