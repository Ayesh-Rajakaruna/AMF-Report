package com.amf.report.constant;

import java.util.Set;

/** Application-wide constants. */
public final class AppConstants {

    private AppConstants() {
    }

    // ---- Outbound HTTP proxy ----
    public static final String PROXY_HOST = "http-gw.tcif.telstra.com.au";
    public static final int PROXY_PORT = 8080;
    public static final String PROXY_USERNAME = "e045577";

    // ---- loyaltyEarn usageCharacteristic names ----
    public static final String CHARACTERISTIC_EARNING_TYPE = "EarningType";
    public static final String CHARACTERISTIC_POLICY_ID = "PolicyId";

    // ---- Earning types ----
    public static final String EARNING_TYPE_MONTHLY = "Monthly";
    public static final String EARNING_TYPE_UPFRONT = "Upfront";

    /** Only these earning types are saved; others (e.g. ManualCredit) are skipped. */
    public static final Set<String> ALLOWED_EARNING_TYPES = Set.of(EARNING_TYPE_MONTHLY, EARNING_TYPE_UPFRONT);

    // ---- loyaltyProgramMember characteristic holding the CIDN ----
    public static final String CHARACTERISTIC_CIDN = "CIDN";

    // ---- loyaltyAccountBalance types summed into the total balance ----
    public static final String BALANCE_TYPE_UPFRONT_TOTAL = "UpfrontBalanceTotal";
    public static final String BALANCE_TYPE_EARNING_TOTAL = "EarningBalanceTotal";
    public static final String BALANCE_TYPE_MANUAL_CREDIT_TOTAL = "ManualCreditBalanceTotal";

    public static final Set<String> TOTAL_BALANCE_TYPES = Set.of(
            BALANCE_TYPE_UPFRONT_TOTAL, BALANCE_TYPE_EARNING_TOTAL, BALANCE_TYPE_MANUAL_CREDIT_TOTAL);
}
