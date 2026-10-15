package com.amf.report.dto;

import com.amf.report.constant.ReportEnvironment;
import lombok.Builder;

/**
 * A generated organization report held in memory (used for download and for saving to disk).
 */
@Builder
public record OrganizationReportFile(
        ReportEnvironment environment,
        String productGroupId,
        String productGroupName,
        String fileName,
        byte[] content,
        int recordCount,
        int notFoundCount) {
}
