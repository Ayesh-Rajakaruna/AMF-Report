package com.amf.report.service;

import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;

import java.util.List;

public interface LoyaltyEarnService {

    /**
     * Takes a loaded loyaltyEarn and saves
     * quantity.value as PLAN_CONTRIBUTION,
     * policyId as POLICY_ID and
     * loyaltyProgramMember.id as LOYALTY_PROGRAM_MEMBER.
     */
    LoyaltyEarnResponseDto saveLoyaltyEarn(LoyaltyEarnDto request);

    /**
     * Loads all loyaltyEarn records from the external TMF API
     * and saves each one to MySQL.
     */
    List<LoyaltyEarnResponseDto> syncLoyaltyEarnsFromApi();

    List<LoyaltyEarnResponseDto> getAllLoyaltyEarns();
}
