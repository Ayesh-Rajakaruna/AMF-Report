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

    /** loyaltyEarn.policyId */
    @Column(name = "policy_id", length = 100)
    private String policyId;

    /** loyaltyEarn.loyaltyProgramMember.id */
    @Column(name = "loyalty_program_member", length = 100)
    private String loyaltyProgramMember;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
