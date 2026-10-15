package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacteristicDto {
    private String name;

    private Object value;

    @JsonIgnore
    public String getValueAsText() {
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return null;
    }

    public static String findValue(List<CharacteristicDto> characteristics, String name) {
        return Optional.ofNullable(characteristics).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .map(CharacteristicDto::getValueAsText)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static String firstValue(List<CharacteristicDto> characteristics) {
        return Optional.ofNullable(characteristics).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(CharacteristicDto::getValueAsText)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
