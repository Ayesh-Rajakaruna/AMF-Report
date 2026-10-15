package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response of GET /journey-service-product-journey/product-journey/product/groups/{productGroupId}.
 * Only the fields the report needs are mapped.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductGroupDto {
    private String id;

    private String name;

    private String description;

    private String channel;

    private List<String> addedCustomers;
}
