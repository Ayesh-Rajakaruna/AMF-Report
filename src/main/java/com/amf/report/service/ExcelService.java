package com.amf.report.service;

import com.amf.report.dto.OrganizationReportRecord;
import com.amf.report.excel.LoyaltyEarnRecordExcelWriter;

import java.nio.file.Path;
import java.util.List;

public interface ExcelService {
    LoyaltyEarnRecordExcelWriter openLoyaltyEarnRecordWriter();

    /**
     * Builds the organization report (.xlsx) in memory.
     */
    byte[] createOrganizationReport(List<OrganizationReportRecord> records);

    /**
     * Saves report content to the configured report.output-dir and returns the file path.
     */
    Path saveReport(String fileName, byte[] content);
}
