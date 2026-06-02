package com.urbanmicrocad;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.urbanmicrocad.**.mapper")
@EnableScheduling
@SpringBootApplication
public class UrbanMicroCadBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrbanMicroCadBackendApplication.class, args);
    }
}
