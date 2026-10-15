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

    @Column(name = "plan_contribution", precision = 19, scale = 2)
    private BigDecimal planContribution;

    @Column(name = "policy_id", length = 100)
    private String policyId;

    @Column(name = "loyalty_program_member", length = 100)
    private String loyaltyProgramMember;

    @Column(name = "earning_type", length = 20)
    private String earningType;

    @Column(name = "policy_name", columnDefinition = "TEXT")
    private String policyName;

    @Column(name = "organization_name", columnDefinition = "TEXT")
    private String organizationName;

    @Column(name = "cidn", length = 50)
    private String cidn;

    @Column(name = "total_balance", precision = 19, scale = 6)
    private BigDecimal totalBalance;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
