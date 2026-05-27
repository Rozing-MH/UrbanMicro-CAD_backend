package com.urbanmicrocad.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI urbanMicroCadOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("UrbanMicro-CAD Backend API")
                .version("0.1.0")
                .description("Backend APIs for UrbanMicro-CAD."));
    }
}
