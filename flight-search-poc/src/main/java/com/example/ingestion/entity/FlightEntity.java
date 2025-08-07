package com.example.ingestion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapped to the "flights" table (see migrations V1__create_flights_table.sql).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "flights")
public class FlightEntity {

  @Id
  @GeneratedValue
  @Column(name = "_id")
  private UUID id;

  @Column(name = "flight_id", nullable = false, unique = true)
  private String flightId;

  private String airline;
  private String departureCity;
  private String arrivalCity;

  private Instant departureTime;

  private BigDecimal basePrice;
}
