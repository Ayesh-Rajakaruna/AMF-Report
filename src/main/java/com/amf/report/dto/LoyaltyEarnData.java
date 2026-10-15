package com.amf.report.dto;

import java.math.BigDecimal;

public record LoyaltyEarnData(BigDecimal planContribution, String policyId, String memberId, String earningType) {
}
