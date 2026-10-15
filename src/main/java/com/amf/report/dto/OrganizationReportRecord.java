package com.amf.report.dto;

import lombok.Builder;

/**
 * One row of the organization report. organizationId is kept for logging; only
 * organizationName and cidn are written to Excel.
 */
@Builder
public record OrganizationReportRecord(
        String organizationId,
        String organizationName,
        String cidn) {
}
