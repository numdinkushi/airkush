package com.kush.csv;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.kush.payload.request.AirportRequest;
import com.kush.payload.request.CityRequest;

@Component
public class LocationCsvMapper {

    public static final int MAX_ROWS = 500;

    public java.util.List<Indexed<CityRequest>> toCityRequests(MultipartFile file) {
        CsvTable table = CsvTable.from(file, MAX_ROWS);
        table.requireColumns("name", "citycode", "countrycode", "countryname");
        return table.rows().stream()
                .map(row -> new Indexed<>(row.lineNumber(), CityRequest.builder()
                        .name(row.get("name"))
                        .cityCode(row.get("citycode"))
                        .countryCode(row.get("countrycode"))
                        .countryName(row.get("countryname"))
                        .regionCode(row.get("regioncode"))
                        .timeZoneOffset(first(row, "timezoneoffset", "timezoneid"))
                        .build()))
                .toList();
    }

    public java.util.List<Indexed<AirportCsvRow>> toAirportRows(MultipartFile file) {
        CsvTable table = CsvTable.from(file, MAX_ROWS);
        table.requireColumns("name", "iatacode", "icaocode");
        if (!table.hasColumn("cityid") && !table.hasColumn("citycode")) {
            throw new com.kush.exception.BadRequestException(
                    "CSV must include cityId or cityCode"
            );
        }
        return table.rows().stream()
                .map(row -> new Indexed<>(row.lineNumber(), new AirportCsvRow(
                        row.get("name"),
                        row.get("iatacode"),
                        row.get("icaocode"),
                        parseLong(row.get("cityid"), row.lineNumber()),
                        row.get("citycode"),
                        first(row, "timezoneid", "timezoneoffset"),
                        parseDouble(row.get("latitude"), row.lineNumber(), "latitude"),
                        parseDouble(row.get("longitude"), row.lineNumber(), "longitude")
                )))
                .toList();
    }

    public AirportRequest toAirportRequest(AirportCsvRow row, Long cityId) {
        return AirportRequest.builder()
                .name(row.name())
                .iataCode(row.iataCode())
                .icaoCode(row.icaoCode())
                .cityId(cityId)
                .timeZoneId(row.timeZoneId())
                .latitude(row.latitude())
                .longitude(row.longitude())
                .build();
    }

    private static String first(CsvTable.Row row, String... columns) {
        for (String column : columns) {
            String value = row.get(column);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Long parseLong(String value, int lineNumber) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new com.kush.exception.BadRequestException(
                    "CSV line " + lineNumber + " has an invalid cityId: " + value
            );
        }
    }

    private static Double parseDouble(String value, int lineNumber, String column) {
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new com.kush.exception.BadRequestException(
                    "CSV line " + lineNumber + " has an invalid " + column + ": " + value
            );
        }
    }

    public record Indexed<T>(int index, T value) {
    }

    public record AirportCsvRow(
            String name,
            String iataCode,
            String icaoCode,
            Long cityId,
            String cityCode,
            String timeZoneId,
            Double latitude,
            Double longitude
    ) {
    }
}
