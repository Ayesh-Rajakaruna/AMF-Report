package com.amf.report.dto;

/**
 * Request body for POST /api/product-groups/organization-report.
 *
 * @param environment    Dev, QA, Pre Prod or Prod (case and spaces are ignored)
 * @param productGroupId product group id, e.g. 54ee4173-a788-449c-b21e-a717bc960427_1744352817967
 */
public record ProductGroupReportRequestDto(String environment, String productGroupId) {
}
