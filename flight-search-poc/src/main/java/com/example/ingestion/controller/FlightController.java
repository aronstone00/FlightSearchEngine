package com.example.ingestion.controller;

import com.example.ingestion.dto.FlightDTO;
import com.example.ingestion.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * REST endpoints for managing flights.
 */
@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

  private static final Logger LOG = LoggerFactory.getLogger(FlightController.class);

  private final FlightService service;

  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody FlightDTO dto) {
    long start = System.currentTimeMillis();
    service.create(dto);
    LOG.info("createFlight took {}ms", System.currentTimeMillis() - start);
    return ResponseEntity.status(CREATED).build();
  }

  @GetMapping("/{flightId}")
  public FlightDTO get(@PathVariable String flightId) {
    return service.get(flightId);
  }
}
