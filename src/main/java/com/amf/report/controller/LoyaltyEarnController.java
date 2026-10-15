package com.amf.report.controller;

import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.service.LoyaltyEarnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty-earn")
@RequiredArgsConstructor
public class LoyaltyEarnController {
    private final LoyaltyEarnService loyaltyEarnService;

    @PostMapping("/sync")
    public ResponseEntity<List<LoyaltyEarnResponseDto>> syncLoyaltyEarns() {
        return ResponseEntity.ok(loyaltyEarnService.syncLoyaltyEarnsFromApi());
    }
}
