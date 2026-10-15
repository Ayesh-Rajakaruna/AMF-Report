package com.amf.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_earn_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyEarnRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** loyaltyEarn.quantity.value */
    @Column(name = "plan_contribution", precision = 19, scale = 2)
    private BigDecimal planContribution;

    /** usageCharacteristic "PolicyId" */
    @Column(name = "policy_id", length = 100)
    private String policyId;

    /** loyaltyEarn.loyaltyProgramMember.id */
    @Column(name = "loyalty_program_member", length = 100)
    private String loyaltyProgramMember;

    /** usageCharacteristic "EarningType" - Monthly or Upfront */
    @Column(name = "earning_type", length = 20)
    private String earningType;

    /** loyaltyRule.policyName, looked up by PolicyId */
    @Column(name = "policy_name", columnDefinition = "TEXT")
    private String policyName;

    /** loyaltyProgramMember.name, looked up by loyaltyProgramMember.id */
    @Column(name = "organization_name", columnDefinition = "TEXT")
    private String organizationName;

    /** loyaltyProgramMember characteristic value (CIDN) */
    @Column(name = "cidn", length = 50)
    private String cidn;

    /** Sum of UpfrontBalanceTotal + EarningBalanceTotal + ManualCreditBalanceTotal */
    @Column(name = "total_balance", precision = 19, scale = 6)
    private BigDecimal totalBalance;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
