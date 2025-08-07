package com.example.ingestion.repository;

import com.example.ingestion.entity.FlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FlightRepository extends JpaRepository<FlightEntity, UUID> {
  Optional<FlightEntity> findByFlightId(String flightId);
}
