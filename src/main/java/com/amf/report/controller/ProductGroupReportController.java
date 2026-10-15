package com.amf.report.controller;

import com.amf.report.dto.ProductGroupReportResponseDto;
import com.amf.report.service.ProductGroupReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.amf.report.constant.AppConstants.DEFAULT_REPORT_ENVIRONMENT;

@RestController
@RequestMapping("/api/product-groups/organization-report")
@RequiredArgsConstructor
public class ProductGroupReportController {
    private final ProductGroupReportService productGroupReportService;

    @PostMapping
    public ResponseEntity<ProductGroupReportResponseDto> generateReport(
            @RequestParam(defaultValue = DEFAULT_REPORT_ENVIRONMENT) String environment,
            @RequestParam String productGroupId) {
        return ResponseEntity.ok(productGroupReportService.generateAndSaveOrganizationReport(environment, productGroupId));
    }
}
