package com.amf.report.service.impl;

import com.amf.report.dto.LoyaltyEarnRecord;
import com.amf.report.excel.LoyaltyEarnRecordExcelWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void writer_appendsBatchesIntoOneFile() throws Exception {
        Path outputDir = tempDir.resolve("reports");
        ExcelServiceImpl excelService = new ExcelServiceImpl(outputDir.toString());
        LoyaltyEarnRecord full = LoyaltyEarnRecord.builder()
                .id("LE-1")
                .planContribution(new BigDecimal("13.825570"))
                .earningType("Monthly")
                .policyName("Gold Policy")
                .organizationName("Acme Pty Ltd")
                .cidn("6781234778")
                .totalBalance(new BigDecimal("154670.155"))
                .build();
        LoyaltyEarnRecord sparse = LoyaltyEarnRecord.builder().id("LE-2").earningType("Upfront").build();
        LoyaltyEarnRecord third = LoyaltyEarnRecord.builder().id("LE-3").earningType("Monthly").build();

        Path file;
        try (LoyaltyEarnRecordExcelWriter writer = excelService.openLoyaltyEarnRecordWriter()) {
            writer.append(List.of(full, sparse));
            writer.append(List.of(third));
            assertThat(writer.getRecordCount()).isEqualTo(3);
            file = writer.finish();
        }

        assertThat(file).exists();
        assertThat(file.getParent()).isEqualTo(outputDir);
        assertThat(file.getFileName().toString()).startsWith("LoyaltyEarnRecord_").endsWith(".xlsx");

        try (InputStream in = Files.newInputStream(file); XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheet("LoyaltyEarnRecord");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getLastRowNum()).isEqualTo(3);

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(header.getCell(6).getStringCellValue()).isEqualTo("Total Balance");
            assertThat(header.getCell(7)).isNull();

            Row row = sheet.getRow(1);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("LE-1");
            assertThat(row.getCell(1).getNumericCellValue()).isEqualTo(13.82557);
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Monthly");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Gold Policy");
            assertThat(row.getCell(4).getStringCellValue()).isEqualTo("Acme Pty Ltd");
            assertThat(row.getCell(5).getStringCellValue()).isEqualTo("6781234778");
            assertThat(row.getCell(6).getNumericCellValue()).isEqualTo(154670.155);

            Row sparseRow = sheet.getRow(2);
            assertThat(sparseRow.getCell(0).getStringCellValue()).isEqualTo("LE-2");
            assertThat(sparseRow.getCell(1)).isNull();

            assertThat(sheet.getRow(3).getCell(0).getStringCellValue()).isEqualTo("LE-3");
        }
    }

    @Test
    void writer_withNoRows_writesHeaderOnly() throws Exception {
        ExcelServiceImpl excelService = new ExcelServiceImpl(tempDir.toString());

        Path file;
        try (LoyaltyEarnRecordExcelWriter writer = excelService.openLoyaltyEarnRecordWriter()) {
            file = writer.finish();
        }

        try (InputStream in = Files.newInputStream(file); XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            assertThat(workbook.getSheet("LoyaltyEarnRecord").getLastRowNum()).isZero();
        }
    }
}
