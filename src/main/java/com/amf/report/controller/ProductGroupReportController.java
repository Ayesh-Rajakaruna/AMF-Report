package com.amf.report.controller;

import com.amf.report.dto.ProductGroupReportRequestDto;
import com.amf.report.dto.ProductGroupReportResponseDto;
import com.amf.report.exception.InvalidRequestException;
import com.amf.report.service.ProductGroupReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-groups/organization-report")
@RequiredArgsConstructor
public class ProductGroupReportController {
    private final ProductGroupReportService productGroupReportService;

    /**
     * Generates the report and saves it to report.output-dir.
     * Body: {"environment": "DEV", "productGroupId": "54ee..._1744352817967"}
     */
    @PostMapping
    public ResponseEntity<ProductGroupReportResponseDto> generateReport(
            @RequestBody(required = false) ProductGroupReportRequestDto request) {
        if (request == null) {
            throw new InvalidRequestException("Request body with environment and productGroupId is required");
        }
        return ResponseEntity.ok(productGroupReportService.generateAndSaveOrganizationReport(
                request.environment(), request.productGroupId()));
    }
}
