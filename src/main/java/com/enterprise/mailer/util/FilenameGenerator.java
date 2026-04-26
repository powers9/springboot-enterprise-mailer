package com.enterprise.mailer.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FilenameGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public String generateFilename(String reportName, String extension) {
        String sanitizedName = reportName.replaceAll("[^a-zA-Z0-9.-]", "_").toLowerCase();
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return String.format("%s_%s.%s", sanitizedName, timestamp, extension);
    }
}
