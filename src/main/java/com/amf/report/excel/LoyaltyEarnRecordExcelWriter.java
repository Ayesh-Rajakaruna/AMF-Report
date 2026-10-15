package com.amf.report.excel;

import com.amf.report.dto.LoyaltyEarnRecord;
import com.amf.report.exception.ExcelGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.amf.report.constant.AppConstants.EXCEL_COLUMN_WIDTH;
import static com.amf.report.constant.AppConstants.EXCEL_DECIMAL_FORMAT;
import static com.amf.report.constant.AppConstants.EXCEL_ROW_ACCESS_WINDOW;
import static com.amf.report.constant.AppConstants.LOYALTY_EARN_RECORD_HEADERS;
import static com.amf.report.constant.AppConstants.LOYALTY_EARN_RECORD_NAME;

@Slf4j
public class LoyaltyEarnRecordExcelWriter implements AutoCloseable {
    private final Path file;
    private final SXSSFWorkbook workbook;
    private final Sheet sheet;
    private final CellStyle decimalStyle;
    private int nextRowIndex = 1;

    public LoyaltyEarnRecordExcelWriter(Path file) {
        this.file = file;
        this.workbook = new SXSSFWorkbook(EXCEL_ROW_ACCESS_WINDOW);
        this.workbook.setCompressTempFiles(true);
        this.sheet = workbook.createSheet(LOYALTY_EARN_RECORD_NAME);
        this.decimalStyle = formatStyle(EXCEL_DECIMAL_FORMAT);
        writeHeader();
    }

    public void append(List<LoyaltyEarnRecord> records) {
        for (LoyaltyEarnRecord record : records) {
            writeRow(sheet.createRow(nextRowIndex++), record);
        }
    }

    public int getRecordCount() {
        return nextRowIndex - 1;
    }

    public Path finish() {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
            log.info("Wrote {} loyaltyEarn record(s) to {}", getRecordCount(), file.toAbsolutePath());
            return file;
        } catch (IOException ex) {
            throw new ExcelGenerationException("Failed to write Excel file " + file + ": " + ex.getMessage(), ex);
        }
    }

    @Override
    public void close() {
        try {
            workbook.close();
        } catch (IOException ex) {
            log.warn("Failed to close Excel workbook {}", file, ex);
        }
    }

    private void writeHeader() {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(font);

        Row header = sheet.createRow(0);
        for (int i = 0; i < LOYALTY_EARN_RECORD_HEADERS.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(LOYALTY_EARN_RECORD_HEADERS.get(i));
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, EXCEL_COLUMN_WIDTH);
        }
        sheet.createFreezePane(0, 1);
    }

    private void writeRow(Row row, LoyaltyEarnRecord record) {
        int col = 0;
        setText(row, col++, record.getId());
        setDecimal(row, col++, record.getPlanContribution());
        setText(row, col++, record.getEarningType());
        setText(row, col++, record.getPolicyName());
        setText(row, col++, record.getOrganizationName());
        setText(row, col++, record.getCidn());
        setDecimal(row, col, record.getTotalBalance());
    }

    private void setText(Row row, int col, String value) {
        if (value != null) {
            row.createCell(col).setCellValue(value);
        }
    }

    private void setDecimal(Row row, int col, BigDecimal value) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(decimalStyle);
        }
    }

    private CellStyle formatStyle(String format) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        return style;
    }
}
