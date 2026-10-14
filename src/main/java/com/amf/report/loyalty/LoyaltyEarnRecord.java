package com.amf.report.loyalty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "loyalty_earn_record")
@Getter
@Setter
public class LoyaltyEarnRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... existing fields ...

    /** loyaltyEarn.quantity.value */
    @Column(name = "plan_contribution", precision = 19, scale = 2)
    private BigDecimal planContribution;

    /** loyaltyEarn.policyId */
    @Column(name = "policy_id", length = 100)
    private String policyId;

    /** loyaltyEarn.loyaltyProgramMember.id */
    @Column(name = "loyalty_program_member", length = 100)
    private String loyaltyProgramMember;
}
