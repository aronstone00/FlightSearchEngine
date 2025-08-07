package com.example.search.controller;

import com.example.search.dto.FlightDTO;
import com.example.search.service.FlightSearchService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SearchController {

  private final FlightSearchService service;

  @GetMapping("/search")
  public PageImpl<FlightDTO> search(
      @RequestParam String departureCity,
      @RequestParam String arrivalCity,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "basePrice,asc") @Pattern(regexp = "^[a-zA-Z]+,(asc|desc)$") String sort) {

    String[] parts = sort.split(",");
    Sort sortObj = Sort.by(Sort.Direction.fromString(parts[1]), parts[0]);
    return service.search(departureCity, arrivalCity, departureDate, page, size, sortObj);
  }
}
