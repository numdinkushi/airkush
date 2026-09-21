package com.kush.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kush.model.Airline;

public interface AirlineRepository extends JpaRepository<Airline, Long> {

    Optional<Airline> findByIataCodeIgnoreCase(String iataCode);

    boolean existsByIataCodeIgnoreCase(String iataCode);

    boolean existsByIataCodeIgnoreCaseAndIdNot(String iataCode, Long id);

    boolean existsByIcaoCodeIgnoreCase(String icaoCode);

    boolean existsByIcaoCodeIgnoreCaseAndIdNot(String icaoCode, Long id);

    boolean existsByOwnerUserId(Long ownerUserId);

    @Query("""
            SELECT a FROM Airline a
            WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(a.iataCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(a.icaoCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Airline> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
