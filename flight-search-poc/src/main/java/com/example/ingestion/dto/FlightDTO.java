package com.example.ingestion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object used both by the Ingestion & Search services.
 * Validation annotations ensure HTTP payloads are checked automatically
 * by Spring's @Valid mechanism.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {

  @NotBlank
  private String flightId;

  @NotBlank
  private String airline;

  @NotBlank
  private String departureCity;

  @NotBlank
  private String arrivalCity;

  @NotNull
  private Instant departureTime; // UTC

  @NotNull
  @DecimalMin("0.0")
  private BigDecimal basePrice;
}
