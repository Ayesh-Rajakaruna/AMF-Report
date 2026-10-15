package com.amf.report.service;

import com.amf.report.dto.LoyaltyEarnResponseDto;

import java.util.List;

public interface LoyaltyEarnService {
    List<LoyaltyEarnResponseDto> syncLoyaltyEarnsFromApi();
}
