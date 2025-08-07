package com.example.ingestion.service;

import com.example.ingestion.dto.FlightDTO;
import com.example.ingestion.entity.FlightEntity;
import com.example.ingestion.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;


import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FlightService {

  private static final Logger LOG = LoggerFactory.getLogger(FlightService.class);
  private static final int MAX_RETRIES = 3;

  private final FlightRepository repository;
  private final RestTemplate restTemplate;
  @org.springframework.beans.factory.annotation.Autowired(required = false)
  private com.example.search.service.FlightSearchService directSearchService;

  @Value("${search.service.url:http://localhost:8082}")
  private String searchServiceUrl;

  /**
   * Creates a new flight if the provided flightId is unique and indexes it in ES.
   *
   * @throws ResponseStatusException 409 if duplicate flightId
   */
  public FlightDTO create(FlightDTO dto) {
    repository.findByFlightId(dto.getFlightId()).ifPresent(f -> {
      throw new ResponseStatusException(CONFLICT, "Duplicate flightId: " + dto.getFlightId());
    });

    repository.save(toEntity(dto));

    // If SearchService bean is present in the same JVM (single-jar mode) call it directly – skips HTTP.
    if (directSearchService != null) {
      retry(() -> directSearchService.index(toSearchDto(dto)));
    } else {
      // Otherwise POST to the separate Search Service (micro-service mode)
      retry(() -> indexIntoSearch(dto));
    }

    return dto;
  }

  public FlightDTO get(String flightId) {
    FlightEntity entity = repository.findByFlightId(flightId)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Flight not found"));
    return toDto(entity);
  }

  /* ------------------------- Private helpers -------------------------- */

  private void indexIntoSearch(FlightDTO dto) {
    String url = searchServiceUrl + "/api/v1/index/flight";
    restTemplate.postForEntity(url, dto, Void.class);
  }

  private void retry(Runnable runnable) {
    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        runnable.run();
        return; // success
      } catch (RestClientException ex) {
        long backoffMs = (long) Math.pow(2, attempt); // exponential backoff 2^n ms
        LOG.warn("Index attempt {} failed ({}). Retrying in {}ms", attempt, ex.getMessage(), backoffMs);
        try {
          TimeUnit.MILLISECONDS.sleep(backoffMs);
        } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
      }
    }
    LOG.error("Failed to index flight after {} attempts – continuing without rollback", MAX_RETRIES);
  }

  private FlightEntity toEntity(FlightDTO dto) {
    return FlightEntity.builder()
        .flightId(dto.getFlightId())
        .airline(dto.getAirline())
        .departureCity(dto.getDepartureCity())
        .arrivalCity(dto.getArrivalCity())
        .departureTime(dto.getDepartureTime())
        .basePrice(dto.getBasePrice())
        .build();
  }

  private com.example.search.dto.FlightDTO toSearchDto(FlightDTO dto) {
    return com.example.search.dto.FlightDTO.builder()
        .flightId(dto.getFlightId())
        .airline(dto.getAirline())
        .departureCity(dto.getDepartureCity())
        .arrivalCity(dto.getArrivalCity())
        .departureTime(dto.getDepartureTime())
        .basePrice(dto.getBasePrice())
        .build();
  }

  private FlightDTO toDto(FlightEntity e) {
    return FlightDTO.builder()
        .flightId(e.getFlightId())
        .airline(e.getAirline())
        .departureCity(e.getDepartureCity())
        .arrivalCity(e.getArrivalCity())
        .departureTime(e.getDepartureTime())
        .basePrice(e.getBasePrice())
        .build();
  }
}
