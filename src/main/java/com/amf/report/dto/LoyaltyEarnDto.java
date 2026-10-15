package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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

    public String getUsageCharacteristicValue(String name) {
        return CharacteristicDto.findValue(usageCharacteristic, name);
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
}
