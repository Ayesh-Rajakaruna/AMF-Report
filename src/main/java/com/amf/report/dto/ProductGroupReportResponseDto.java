package com.amf.report.dto;

public record ProductGroupReportResponseDto(
        String environment,
        String productGroupId,
        String productGroupName,
        String fileName,
        String filePath,
        int recordCount,
        int notFoundCount) {
}
