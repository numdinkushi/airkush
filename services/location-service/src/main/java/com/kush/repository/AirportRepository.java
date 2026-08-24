package com.kush.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kush.model.Airport;

public interface AirportRepository extends JpaRepository<Airport, Long> {

    boolean existsByIataCode(String iataCode);

    boolean existsByIcaoCode(String icaoCode);

    boolean existsByIataCodeAndIdNot(String iataCode, Long id);

    boolean existsByIcaoCodeAndIdNot(String icaoCode, Long id);

    boolean existsByCityId(Long cityId);

    Optional<Airport> findByIataCodeIgnoreCase(String iataCode);

    Page<Airport> findByCityId(Long cityId, Pageable pageable);

    @Query("""
            select a from Airport a
            where lower(a.name) like lower(concat('%', :keyword, '%'))
               or lower(a.iataCode) like lower(concat('%', :keyword, '%'))
               or lower(a.icaoCode) like lower(concat('%', :keyword, '%'))
               or lower(a.city.name) like lower(concat('%', :keyword, '%'))
               or lower(a.city.cityCode) like lower(concat('%', :keyword, '%'))
            """)
    Page<Airport> searchByKeyword(String keyword, Pageable pageable);
}
