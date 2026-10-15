package com.amf.report.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * TMF LoyaltyAccountBalance, e.g.
 * <pre>
 * {
 *   "balanceType": "UpfrontBalanceTotal",
 *   "relatedAccount": { "id": "1e518a3d-..._1747658990003", "name": "6781234778" },
 *   "amount": { "unit": "AUD", "value": 6402.11 }
 * }
 * </pre>
 * {@code relatedAccount.name} is the CIDN; balances are summed per CIDN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoyaltyAccountBalanceDto {

    private String id;

    private String balanceType;

    private RelatedAccountDto relatedAccount;

    private AmountDto amount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelatedAccountDto {
        private String id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AmountDto {
        private BigDecimal value;
    }
}
