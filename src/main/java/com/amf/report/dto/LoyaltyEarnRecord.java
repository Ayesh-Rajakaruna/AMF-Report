package com.amf.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyEarnRecord {

    private BigDecimal planContribution;

    private String earningType;

    private String policyName;

    private String organizationName;

    private String cidn;

    private BigDecimal totalBalance;

    private String agreementName;
}
