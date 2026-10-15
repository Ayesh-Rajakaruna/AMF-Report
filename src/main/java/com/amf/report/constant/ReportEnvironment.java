package com.amf.report.constant;

import com.amf.report.exception.InvalidRequestException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum ReportEnvironment {
    DEV("Dev"),
    QA("QA"),
    PREPROD("Pre Prod"),
    PROD("Prod");

    private final String label;

    public static ReportEnvironment fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException("environment is required. Allowed values: " + allowedValues());
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replaceAll("[\\s_-]", "");
        return Arrays.stream(values())
                .filter(env -> env.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(
                        "Invalid environment '" + value + "'. Allowed values: " + allowedValues()));
    }

    private static String allowedValues() {
        return Arrays.stream(values()).map(ReportEnvironment::getLabel).collect(Collectors.joining(", "));
    }
}
