package com.amf.report.repository;

import com.amf.report.entity.LoyaltyEarnRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyEarnRecordRepository extends JpaRepository<LoyaltyEarnRecord, Long> {
}
