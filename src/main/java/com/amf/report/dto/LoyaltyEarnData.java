package com.amf.report.dto;

import java.math.BigDecimal;

public record LoyaltyEarnData(String id, BigDecimal planContribution, String policyId, String memberId, String earningType,
                              String agreementName) {
}
