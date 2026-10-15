package com.amf.report.service;

import com.amf.report.dto.OrganizationReportFile;
import com.amf.report.dto.ProductGroupReportResponseDto;

public interface ProductGroupReportService {

    /**
     * Builds the organization report for a product group and returns it in memory (for download).
     *
     * @param environment    Dev, QA, Pre Prod or Prod
     * @param productGroupId product group id
     */
    OrganizationReportFile createOrganizationReport(String environment, String productGroupId);

    /**
     * Builds the organization report and saves it to report.output-dir.
     */
    ProductGroupReportResponseDto generateAndSaveOrganizationReport(String environment, String productGroupId);
}
