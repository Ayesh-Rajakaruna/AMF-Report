package com.amf.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyEarnResponseDto {
    private String id;
    private BigDecimal planContribution;
    private String earningType;
    private String policyName;
    private String organizationName;
    private String cidn;
    private BigDecimal totalBalance;
    private LocalDateTime createdAt;
}
