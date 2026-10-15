package com.amf.report.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record LoyaltyEarnData(
        String id,
        BigDecimal planContribution,
        String policyId,
        String memberId,
        String earningType,
        String agreementName) {
}
