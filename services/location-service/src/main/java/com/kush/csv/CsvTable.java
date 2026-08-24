package com.kush.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.kush.exception.BadRequestException;

public final class CsvTable {

    private final List<String> headers;
    private final List<Row> rows;

    private CsvTable(List<String> headers, List<Row> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public List<Row> rows() {
        return rows;
    }

    public static CsvTable from(MultipartFile file, int maxRows) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required");
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new BadRequestException("File must be a .csv");
        }

        try (InputStream input = file.getInputStream()) {
            return parse(input, maxRows);
        } catch (IOException ex) {
            throw new BadRequestException("Could not read CSV file");
        }
    }

    static CsvTable parse(InputStream input, int maxRows) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String headerLine = reader.readLine();
        if (headerLine != null && headerLine.startsWith("\uFEFF")) {
            headerLine = headerLine.substring(1);
        }
        if (headerLine == null || headerLine.isBlank()) {
            throw new BadRequestException("CSV file is empty");
        }

        List<String> headers = parseLine(headerLine).stream()
                .map(header -> header.trim().toLowerCase(Locale.ROOT))
                .toList();
        if (headers.stream().anyMatch(String::isBlank)) {
            throw new BadRequestException("CSV header contains an empty column name");
        }

        List<Row> rows = new ArrayList<>();
        String line;
        int lineNumber = 1;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            List<String> values = parseLine(line);
            if (values.size() != headers.size()) {
                throw new BadRequestException(
                        "CSV line " + lineNumber + " has " + values.size()
                                + " columns but header has " + headers.size()
                );
            }
            if (rows.size() >= maxRows) {
                throw new BadRequestException("CSV cannot contain more than " + maxRows + " data rows");
            }
            Map<String, String> columns = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                columns.put(headers.get(i), blankToNull(values.get(i)));
            }
            rows.add(new Row(lineNumber, columns));
        }
        if (rows.isEmpty()) {
            throw new BadRequestException("CSV has a header but no data rows");
        }
        return new CsvTable(headers, rows);
    }

    public boolean hasColumn(String name) {
        return headers.contains(name.toLowerCase(Locale.ROOT));
    }

    public void requireColumns(String... names) {
        List<String> missing = new ArrayList<>();
        for (String name : names) {
            if (!hasColumn(name)) {
                missing.add(name);
            }
        }
        if (!missing.isEmpty()) {
            throw new BadRequestException("CSV is missing required columns: " + String.join(", ", missing));
        }
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(ch);
                }
            } else if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }

    public record Row(int lineNumber, Map<String, String> columns) {
        public String get(String column) {
            return columns.get(column.toLowerCase(Locale.ROOT));
        }
    }
}
