package com.amf.report.service;

import com.amf.report.excel.LoyaltyEarnRecordExcelWriter;

public interface ExcelService {
    LoyaltyEarnRecordExcelWriter openLoyaltyEarnRecordWriter();
}
