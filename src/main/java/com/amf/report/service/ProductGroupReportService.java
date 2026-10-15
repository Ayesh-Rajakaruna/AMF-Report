package com.amf.report.service;

import com.amf.report.dto.ProductGroupReportResponseDto;

public interface ProductGroupReportService {

    /**
     * Builds the organization report and saves it to report.output-dir.
     *
     * @param environment    Dev, QA, Pre Prod or Prod
     * @param productGroupId product group id
     */
    ProductGroupReportResponseDto generateAndSaveOrganizationReport(String environment, String productGroupId);
}
