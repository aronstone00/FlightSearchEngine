package com.example.search.dto;

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
 * Same DTO definition as Ingestion Service (duplicated here for brevity).
 * In a real-world project we would extract this into a separate shared module.
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
  private Instant departureTime;

  @NotNull
  @DecimalMin("0.0")
  private BigDecimal basePrice;
}
