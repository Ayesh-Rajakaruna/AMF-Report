package com.amf.report.service;

import com.amf.report.dto.ProductGroupReportResponseDto;

public interface ProductGroupReportService {
    ProductGroupReportResponseDto generateAndSaveOrganizationReport(String environment, String productGroupId);
}
