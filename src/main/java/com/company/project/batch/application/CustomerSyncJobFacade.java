package com.company.project.batch.application;

import com.company.project.batch.domain.BatchExecutionRule;
import com.company.project.batch.infrastructure.job.CustomerSyncJobRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerSyncJobFacade {

    private final BatchExecutionRule batchExecutionRule;
    private final CustomerSyncJobRunner customerSyncJobRunner;

    public void trigger() {
        batchExecutionRule.ensureAllowed();
        customerSyncJobRunner.run();
    }
}
