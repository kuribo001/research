package com.company.project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI projectOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Project Backend API")
                .version("v1")
                .description("Backend skeleton aligned with the architecture guideline."));
    }
}
