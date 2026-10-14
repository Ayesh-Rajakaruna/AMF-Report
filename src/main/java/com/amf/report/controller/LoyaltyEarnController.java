package com.amf.report.controller;

import com.amf.report.dto.LoyaltyEarnDto;
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
            @Valid @RequestBody LoyaltyEarnDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loyaltyEarnService.saveLoyaltyEarn(request));
    }

    /** Calls the external loyaltyEarn TMF API and saves all records to MySQL. */
    @PostMapping("/sync")
    public ResponseEntity<List<LoyaltyEarnResponseDto>> syncLoyaltyEarns() {
        return ResponseEntity.ok(loyaltyEarnService.syncLoyaltyEarnsFromApi());
    }

    @GetMapping
    public ResponseEntity<List<LoyaltyEarnResponseDto>> getAllLoyaltyEarns() {
        return ResponseEntity.ok(loyaltyEarnService.getAllLoyaltyEarns());
    }
}
