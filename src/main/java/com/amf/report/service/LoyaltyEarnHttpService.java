package com.amf.report.service;

import com.amf.report.dto.LoyaltyEarnDto;

import java.util.List;

/** HTTP client for the external loyaltyEarn TMF API. */
public interface LoyaltyEarnHttpService {

    /** GET {loyalty.api.loyalty-earn-url} - returns all loyaltyEarn records. */
    List<LoyaltyEarnDto> getAllLoyaltyEarns();
}
