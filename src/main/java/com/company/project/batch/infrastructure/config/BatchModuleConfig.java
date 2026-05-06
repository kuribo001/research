package com.company.project.batch.infrastructure.config;

import com.company.project.batch.domain.BatchExecutionRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchModuleConfig {

    @Bean
    public BatchExecutionRule batchExecutionRule() {
        return new BatchExecutionRule();
    }
}
