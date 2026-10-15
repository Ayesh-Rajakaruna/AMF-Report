package com.amf.report.constant;

import com.amf.report.dto.LoyaltyAccountBalanceDto;
import com.amf.report.dto.LoyaltyEarnDto;
import com.amf.report.dto.LoyaltyRuleDto;
import com.amf.report.dto.ProgramMemberDto;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Set;

public final class AppConstants {
    private AppConstants() {
    }

    public static final String PROXY_HOST = "http-gw.tcif.telstra.com.au";
    public static final int PROXY_PORT = 8080;
    public static final String PROXY_USERNAME = "e045577";

    public static final String CHARACTERISTIC_EARNING_TYPE = "EarningType";
    public static final String CHARACTERISTIC_POLICY_ID = "PolicyId";

    public static final String EARNING_TYPE_MONTHLY = "Monthly";
    public static final String EARNING_TYPE_UPFRONT = "Upfront";

    public static final Set<String> ALLOWED_EARNING_TYPES = Set.of(EARNING_TYPE_MONTHLY, EARNING_TYPE_UPFRONT);

    public static final String CHARACTERISTIC_CIDN = "CIDN";

    public static final String BALANCE_TYPE_UPFRONT_TOTAL = "UpfrontBalanceTotal";
    public static final String BALANCE_TYPE_EARNING_TOTAL = "EarningBalanceTotal";
    public static final String BALANCE_TYPE_MANUAL_CREDIT_TOTAL = "ManualCreditBalanceTotal";

    public static final Set<String> TOTAL_BALANCE_TYPES = Set.of(
            BALANCE_TYPE_UPFRONT_TOTAL, BALANCE_TYPE_EARNING_TOTAL, BALANCE_TYPE_MANUAL_CREDIT_TOTAL);

    public static final ParameterizedTypeReference<List<LoyaltyEarnDto>> LOYALTY_EARN_LIST =
            new ParameterizedTypeReference<>() {};
    public static final ParameterizedTypeReference<List<LoyaltyRuleDto>> LOYALTY_RULE_LIST =
            new ParameterizedTypeReference<>() {};
    public static final ParameterizedTypeReference<List<ProgramMemberDto>> PROGRAM_MEMBER_LIST =
            new ParameterizedTypeReference<>() {};
    public static final ParameterizedTypeReference<List<LoyaltyAccountBalanceDto>> ACCOUNT_BALANCE_LIST =
            new ParameterizedTypeReference<>() {};
}
