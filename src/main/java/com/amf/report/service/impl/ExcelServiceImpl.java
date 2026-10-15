package com.amf.report.service.impl;

import com.amf.report.excel.LoyaltyEarnRecordExcelWriter;
import com.amf.report.service.ExcelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.amf.report.constant.AppConstants.EXCEL_FILE_EXTENSION;
import static com.amf.report.constant.AppConstants.EXCEL_FILE_TIMESTAMP_PATTERN;
import static com.amf.report.constant.AppConstants.LOYALTY_EARN_RECORD_NAME;

@Service
public class ExcelServiceImpl implements ExcelService {
    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern(EXCEL_FILE_TIMESTAMP_PATTERN);

    private final Path outputDir;

    public ExcelServiceImpl(@Value("${report.output-dir}") String outputDir) {
        this.outputDir = Paths.get(outputDir);
    }

    @Override
    public LoyaltyEarnRecordExcelWriter openLoyaltyEarnRecordWriter() {
        Path file = outputDir.resolve(
                LOYALTY_EARN_RECORD_NAME + "_" + LocalDateTime.now().format(FILE_TIMESTAMP) + EXCEL_FILE_EXTENSION);
        return new LoyaltyEarnRecordExcelWriter(file);
    }
}
