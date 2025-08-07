package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal OpenAPI configuration. Springdoc automatically scans all controllers
 * under the base package (`com.example.*`) and exposes the interactive Swagger
 * UI at:
 *   http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI flightSearchOpenAPI() {
    return new OpenAPI().info(new Info()
        .title("Flight Search POC API")
        .description("REST APIs for ingesting and searching flights")
        .version("1.0.0"));
  }
}

