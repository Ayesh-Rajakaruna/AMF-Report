package com.amf.report.service;

import com.amf.report.dto.LoyaltyEarnResponseDto;

import java.util.List;

public interface LoyaltyEarnService {

    /**
     * Loads all loyaltyEarn records from the external TMF API and saves each one to MySQL:
     * quantity.value as PLAN_CONTRIBUTION,
     * policyId as POLICY_ID and
     * loyaltyProgramMember.id as LOYALTY_PROGRAM_MEMBER.
     */
    List<LoyaltyEarnResponseDto> syncLoyaltyEarnsFromApi();
}
