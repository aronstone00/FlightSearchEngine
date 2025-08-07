package com.example.search.service;

import com.example.search.dto.FlightDTO;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightSearchService {
  private static final String INDEX_NAME = "flights";

  private final RestHighLevelClient client;

  /**
   * Upserts the provided flight into Elasticsearch. The document ID is the flightId
   * guaranteeing idempotency.
   */
  public void index(FlightDTO dto) {
    try {
      String docId = dto.getFlightId();

      // Normalize route fields to lowercase for exact-match keyword queries
      java.util.Map<String, Object> doc = new java.util.HashMap<>();
      doc.put("flightId", dto.getFlightId());
      doc.put("airline", dto.getAirline());
      doc.put("departureCity", dto.getDepartureCity().toLowerCase());
      doc.put("arrivalCity", dto.getArrivalCity().toLowerCase());
      doc.put("departureTime", dto.getDepartureTime().toString()); // ISO-8601 string
      doc.put("basePrice", dto.getBasePrice());

      UpdateRequest update = new UpdateRequest(INDEX_NAME, docId)
          .doc(doc)
          .docAsUpsert(true);
      client.update(update, RequestOptions.DEFAULT);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to index flight", ex);
    }
  }

  /**
   * Searches flights by route & date with pagination and sorting.
   */
  public PageImpl<FlightDTO> search(String departureCity,
                                   String arrivalCity,
                                   LocalDate departureDate,
                                   int page,
                                   int size,
                                   Sort sort) {
    Pageable pageable = PageRequest.of(page, size, sort);

    // Calculate UTC millis boundaries for the given departure date
    long startOfDayMillis = departureDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    long nextDayMillis = departureDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

    BoolQueryBuilder bool = QueryBuilders.boolQuery()
        .must(QueryBuilders.termQuery("departureCity", departureCity.toLowerCase()))
        .must(QueryBuilders.termQuery("arrivalCity", arrivalCity.toLowerCase()))
        .must(QueryBuilders.rangeQuery("departureTime")
            .gte(startOfDayMillis)
            .lt(nextDayMillis));

    SearchSourceBuilder source = new SearchSourceBuilder()
        .query(bool)
        .from((int) pageable.getOffset())
        .size(pageable.getPageSize());

    // Only support single property sort: "field,asc|desc"
    if (!sort.isUnsorted()) {
      Sort.Order order = sort.iterator().next();
      source.sort(order.getProperty(), order.isAscending() ? SortOrder.ASC : SortOrder.DESC);
    }

    SearchRequest request = new SearchRequest(INDEX_NAME).source(source);

    try {
      SearchResponse response = client.search(request, RequestOptions.DEFAULT);
      List<FlightDTO> flights = new ArrayList<>();
      for (SearchHit hit : response.getHits()) {
        flights.add(mapper().readValue(hit.getSourceAsString(), FlightDTO.class));
      }
      long totalHits = response.getHits().getTotalHits().value;
      return new PageImpl<>(flights, pageable, totalHits);
    } catch (IOException ex) {
      throw new RuntimeException("Search request failed", ex);
    }
  }

  /* Minimal helper for (de)serialisation */
  private static class Json {
    private static final com.fasterxml.jackson.databind.ObjectMapper INSTANCE = new com.fasterxml.jackson.databind.ObjectMapper()
        .findAndRegisterModules();
  }

  private static com.fasterxml.jackson.databind.ObjectMapper mapper() { return Json.INSTANCE; }
}
