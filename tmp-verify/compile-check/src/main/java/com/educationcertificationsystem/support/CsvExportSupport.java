package com.educationcertificationsystem.support;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvExportSupport {

    private CsvExportSupport() {
    }

    public static ResponseEntity<ByteArrayResource> csv(String fileName, List<String> headers, List<List<?>> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        writeRow(builder, headers);
        for (List<?> row : rows) {
            writeRow(builder, row);
        }
        byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .contentLength(bytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(new ByteArrayResource(bytes));
    }

    private static void writeRow(StringBuilder builder, List<?> row) {
        for (int i = 0; i < row.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(escape(row.get(i)));
        }
        builder.append("\r\n");
    }

    private static String escape(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\r") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
