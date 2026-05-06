package com.company.project.batch.infrastructure.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomerSyncJobRunner {

    private static final Logger log = LoggerFactory.getLogger(CustomerSyncJobRunner.class);

    public void run() {
        log.info("Customer sync batch job triggered");
    }
}
