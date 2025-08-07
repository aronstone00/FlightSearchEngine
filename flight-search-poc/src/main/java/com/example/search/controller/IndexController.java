package com.example.search.controller;

import com.example.search.dto.FlightDTO;
import com.example.search.service.FlightSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/index")
@RequiredArgsConstructor
public class IndexController {

  private final FlightSearchService service;

  /**
   * Synchronous, idempotent upsert by flightId.
   */
  @PostMapping("/flight")
  public ResponseEntity<Void> indexFlight(@Valid @RequestBody FlightDTO dto) {
    service.index(dto);
    return ResponseEntity.ok().build();
  }
}
