package com.amf.report.service;

import com.amf.report.entity.LoyaltyEarnRecord;

import java.nio.file.Path;
import java.util.List;

public interface ExcelService {
    Path writeLoyaltyEarnRecords(List<LoyaltyEarnRecord> records);
}
