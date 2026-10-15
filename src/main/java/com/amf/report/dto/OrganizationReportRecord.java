package com.amf.report.dto;

import lombok.Builder;

@Builder
public record OrganizationReportRecord(
        String organizationId,
        String organizationName,
        String cidn) {
}
