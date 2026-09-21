package com.kush.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kush.enums.FlightStatus;
import com.kush.model.Flight;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    boolean existsByAirlineId(Long airlineId);

    boolean existsByAirlineIdAndFlightNumberIgnoreCaseAndDepartureTime(
            Long airlineId,
            String flightNumber,
            LocalDateTime departureTime
    );

    boolean existsByAirlineIdAndFlightNumberIgnoreCaseAndDepartureTimeAndIdNot(
            Long airlineId,
            String flightNumber,
            LocalDateTime departureTime,
            Long id
    );

    Page<Flight> findByAirlineId(Long airlineId, Pageable pageable);

    @Query("""
            SELECT f FROM Flight f
            WHERE (:origin IS NULL OR f.originIata = :origin)
              AND (:destination IS NULL OR f.destinationIata = :destination)
              AND (:airlineId IS NULL OR f.airline.id = :airlineId)
              AND (:status IS NULL OR f.status = :status)
              AND (:date IS NULL OR CAST(f.departureTime AS date) = :date)
              AND (:keyword IS NULL
                   OR LOWER(f.flightNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(f.airline.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(f.airline.iataCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Flight> search(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("airlineId") Long airlineId,
            @Param("status") FlightStatus status,
            @Param("date") LocalDate date,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
