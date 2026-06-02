package com.urbanmicrocad.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 * <p>
 * Disabled in production by setting {@code springdoc.api-docs.enabled=false}
 * and {@code springdoc.swagger-ui.enabled=false} in application-prod.yml.
 * Default (dev) profile keeps both enabled for local development.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    @ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
    public OpenAPI urbanMicroCadOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("UrbanMicro-CAD Backend API")
                .version("0.1.0")
                .description("Backend APIs for UrbanMicro-CAD."));
    }
}
