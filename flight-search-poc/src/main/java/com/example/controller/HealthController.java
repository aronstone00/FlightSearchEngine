package com.example.controller;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple liveness endpoint complementary to Actuator health checks. Useful for
 * container orchestrators that need a lightweight ping endpoint without any
 * authentication.
 */
@RestController
@RequestMapping("/healthz")
public class HealthController {

  private final JdbcTemplate jdbc;
  private final RestHighLevelClient esClient;

  public HealthController(JdbcTemplate jdbc, RestHighLevelClient esClient) {
    this.jdbc = jdbc;
    this.esClient = esClient;
  }

  @GetMapping
  public java.util.Map<String, Object> ok() {
    boolean pgUp = false;
    boolean esUp = false;
    try {
      pgUp = Boolean.TRUE.equals(jdbc.queryForObject("SELECT 1", Boolean.class));
    } catch (Exception ignored) { }

    try {
      esUp = esClient.ping(RequestOptions.DEFAULT);
    } catch (Exception ignored) { }

    String status = (pgUp && esUp) ? "UP" : "DEGRADED";
    return java.util.Map.of(
        "status", status,
        "postgres", pgUp ? "UP" : "DOWN",
        "elasticsearch", esUp ? "UP" : "DOWN");
  }
}

