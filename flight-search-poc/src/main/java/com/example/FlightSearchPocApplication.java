package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Central entry-point that boots **both** Ingestion & Search components in a single
 * Spring context.  Useful for local development / demos where running two JVMs is
 * inconvenient.  All `@Component`-scanning is rooted at `com.example` so beans from
 * `ingestion-service` and `search-service` are auto-detected.
 *
 * NOTE:
 * • Port defaults to 8080 – override with `SERVER_PORT` or `--server.port=`.
 * • Ensure PostgreSQL **and** Elasticsearch creds are present because both modules
 *   initialise at startup.
 * • If you still prefer true microservices, simply start the individual jars instead.
 */
@SpringBootApplication(scanBasePackages = "com.example")
public class FlightSearchPocApplication {

  public static void main(String[] args) {
    SpringApplication.run(FlightSearchPocApplication.class, args);
  }
}

