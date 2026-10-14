package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TMF LoyaltyRule (policy). Its {@code id} is the loyaltyEarn "PolicyId" characteristic.
 * <pre>{ "id": "90b882f9-..._1790221916661", "policyName": "test new dk policy123", ... }</pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoyaltyRuleDto {

    private String id;

    private String policyName;
}
