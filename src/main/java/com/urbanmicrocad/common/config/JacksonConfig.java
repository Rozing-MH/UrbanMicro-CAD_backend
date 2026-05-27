package com.urbanmicrocad.common.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        StreamReadConstraints constraints = StreamReadConstraints.builder()
            .maxNestingDepth(100)
            .maxStringLength(2_000_000)
            .maxNumberLength(1000)
            .build();
        return builder -> builder
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .postConfigurer(objectMapper -> objectMapper.getFactory().setStreamReadConstraints(constraints));
    }
}
