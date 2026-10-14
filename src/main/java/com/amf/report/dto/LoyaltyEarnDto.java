package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * TMF LoyaltyEarn resource (only the fields this service uses), e.g.
 * <pre>
 * {
 *   "id": "18e278f7-..._1790221917153",
 *   "quantity": { "unit": "AUD", "value": 13.825570 },
 *   "loyaltyProgramMember": { "id": "8e28ac9f-..._1789555298956" },
 *   "usageCharacteristic": [
 *     { "name": "EarningType", "valueType": "String", "value": "Monthly" },
 *     { "name": "PolicyId",    "valueType": "String", "value": "0d4e7ea5-..." }
 *   ]
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoyaltyEarnDto {

    private String id;

    private QuantityDto quantity;

    private LoyaltyProgramMemberDto loyaltyProgramMember;

    private List<CharacteristicDto> usageCharacteristic;

    /** Value of the usageCharacteristic with the given name (case-insensitive), or null. */
    public String getUsageCharacteristicValue(String name) {
        return Optional.ofNullable(usageCharacteristic).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .map(CharacteristicDto::getValue)
                .findFirst()
                .orElse(null);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuantityDto {
        private BigDecimal value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoyaltyProgramMemberDto {
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CharacteristicDto {
        private String name;
        private String value;
    }
}
