package com.amf.report.controller;

import com.amf.report.dto.LoyaltyEarnRequestDto;
import com.amf.report.dto.LoyaltyEarnResponseDto;
import com.amf.report.service.LoyaltyEarnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty-earn")
@RequiredArgsConstructor
public class LoyaltyEarnController {

    private final LoyaltyEarnService loyaltyEarnService;

    @PostMapping
    public ResponseEntity<LoyaltyEarnResponseDto> saveLoyaltyEarn(
            @Valid @RequestBody LoyaltyEarnRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loyaltyEarnService.saveLoyaltyEarn(request));
    }

    @GetMapping
    public ResponseEntity<List<LoyaltyEarnResponseDto>> getAllLoyaltyEarns() {
        return ResponseEntity.ok(loyaltyEarnService.getAllLoyaltyEarns());
    }
}
