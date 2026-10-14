package com.amf.report.service;

import com.amf.report.dto.LoyaltyEarnRequestDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;

import java.util.List;

public interface LoyaltyEarnService {

    /**
     * Takes a loaded loyaltyEarn and saves
     * quantity.value as PLAN_CONTRIBUTION,
     * policyId as POLICY_ID and
     * loyaltyProgramMember.id as LOYALTY_PROGRAM_MEMBER.
     */
    LoyaltyEarnResponseDto saveLoyaltyEarn(LoyaltyEarnRequestDto request);

    List<LoyaltyEarnResponseDto> getAllLoyaltyEarns();
}
