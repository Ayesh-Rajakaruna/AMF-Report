package com.amf.report.dto;

/**
 * Result of a saved organization report.
 *
 * @param recordCount   rows written (always equals the number of addedCustomers)
 * @param notFoundCount addedCustomers the organization API did not return (written as blank rows)
 */
public record ProductGroupReportResponseDto(
        String environment,
        String productGroupId,
        String productGroupName,
        String fileName,
        String filePath,
        int recordCount,
        int notFoundCount) {
}
