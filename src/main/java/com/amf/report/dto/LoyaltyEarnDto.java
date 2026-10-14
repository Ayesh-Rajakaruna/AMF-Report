package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * TMF LoyaltyEarn resource (only the fields this service uses), e.g.
 * <pre>
 * {
 *   "id": "LE-1",
 *   "quantity": { "value": 25.50 },
 *   "policyId": "POL-1001",
 *   "loyaltyProgramMember": { "id": "LPM-2001" }
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

    @Valid
    private QuantityDto quantity;

    private String policyId;

    @Valid
    private LoyaltyProgramMemberDto loyaltyProgramMember;

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
