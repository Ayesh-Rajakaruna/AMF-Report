package com.amf.report.controller;

import com.amf.report.dto.OrganizationReportFile;
import com.amf.report.dto.ProductGroupReportRequestDto;
import com.amf.report.dto.ProductGroupReportResponseDto;
import com.amf.report.exception.InvalidRequestException;
import com.amf.report.service.ProductGroupReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.amf.report.constant.AppConstants.XLSX_CONTENT_TYPE;

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

    /**
     * Generates the report and returns it as an Excel download.
     * Example: GET /api/product-groups/organization-report/download?environment=DEV&productGroupId=54ee..._1744352817967
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadReport(@RequestParam String environment,
                                                 @RequestParam String productGroupId) {
        OrganizationReportFile report = productGroupReportService.createOrganizationReport(environment, productGroupId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(report.fileName()).build().toString())
                .header("X-Record-Count", String.valueOf(report.recordCount()))
                .header("X-Not-Found-Count", String.valueOf(report.notFoundCount()))
                .contentLength(report.content().length)
                .body(report.content());
    }
}
