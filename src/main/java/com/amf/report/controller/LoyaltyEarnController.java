package com.amf.report.controller;

import com.amf.report.dto.LoyaltyEarnReportResponseDto;
import com.amf.report.service.LoyaltyEarnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.amf.report.constant.AppConstants.DEFAULT_REPORT_ENVIRONMENT;

@RestController
@RequestMapping("/api/loyalty-earn")
@RequiredArgsConstructor
public class LoyaltyEarnController {
    private final LoyaltyEarnService loyaltyEarnService;

    @PostMapping("/sync")
    public ResponseEntity<LoyaltyEarnReportResponseDto> syncLoyaltyEarns(
            @RequestParam(defaultValue = DEFAULT_REPORT_ENVIRONMENT) String environment) {
        return ResponseEntity.ok(loyaltyEarnService.syncLoyaltyEarnsFromApi(environment));
    }
}
