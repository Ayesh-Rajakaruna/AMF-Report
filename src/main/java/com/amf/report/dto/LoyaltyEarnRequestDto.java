package com.amf.report.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Incoming loyaltyEarn payload, e.g.
 * <pre>
 * {
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
public class LoyaltyEarnRequestDto {

    @Valid
    private QuantityDto quantity;

    private String policyId;

    @Valid
    private LoyaltyProgramMemberDto loyaltyProgramMember;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantityDto {
        private BigDecimal value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoyaltyProgramMemberDto {
        private String id;
    }
}
