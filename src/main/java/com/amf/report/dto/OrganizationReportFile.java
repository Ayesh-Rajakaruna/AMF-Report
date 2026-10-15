package com.amf.report.dto;

import com.amf.report.constant.ReportEnvironment;
import lombok.Builder;

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
