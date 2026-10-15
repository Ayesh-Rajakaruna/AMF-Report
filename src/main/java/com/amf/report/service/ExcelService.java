package com.amf.report.service;

import com.amf.report.constant.ReportEnvironment;
import com.amf.report.dto.OrganizationReportRecord;
import com.amf.report.excel.LoyaltyEarnRecordExcelWriter;

import java.nio.file.Path;
import java.util.List;

public interface ExcelService {
    LoyaltyEarnRecordExcelWriter openLoyaltyEarnRecordWriter(ReportEnvironment environment);

    byte[] createOrganizationReport(List<OrganizationReportRecord> records);

    Path saveReport(String fileName, byte[] content);
}
