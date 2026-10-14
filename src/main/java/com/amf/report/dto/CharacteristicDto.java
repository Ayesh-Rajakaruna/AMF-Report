package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** TMF characteristic: { "name": "...", "valueType": "String", "value": ... }. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacteristicDto {

    private String name;

    /**
     * Usually a string, but some characteristics (e.g. ObjectCharacteristic)
     * carry a JSON object or array, so this is kept as a raw Object.
     */
    private Object value;

    /** The value as text when it is a simple value (string, number, boolean), otherwise null. */
    @JsonIgnore
    public String getValueAsText() {
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return null;
    }

    /** Text value of the characteristic with the given name (case-insensitive), or null. */
    public static String findValue(List<CharacteristicDto> characteristics, String name) {
        return Optional.ofNullable(characteristics).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .map(CharacteristicDto::getValueAsText)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /** Text value of the first characteristic that has a simple value, or null. */
    public static String firstValue(List<CharacteristicDto> characteristics) {
        return Optional.ofNullable(characteristics).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(CharacteristicDto::getValueAsText)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
