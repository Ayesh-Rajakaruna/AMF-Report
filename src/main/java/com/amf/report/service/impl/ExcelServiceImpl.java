package com.amf.report.service.impl;

import com.amf.report.entity.LoyaltyEarnRecord;
import com.amf.report.exception.ExcelGenerationException;
import com.amf.report.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.amf.report.constant.AppConstants.EXCEL_COLUMN_WIDTH;
import static com.amf.report.constant.AppConstants.EXCEL_DATE_TIME_FORMAT;
import static com.amf.report.constant.AppConstants.EXCEL_DECIMAL_FORMAT;
import static com.amf.report.constant.AppConstants.EXCEL_FILE_EXTENSION;
import static com.amf.report.constant.AppConstants.EXCEL_FILE_TIMESTAMP_PATTERN;
import static com.amf.report.constant.AppConstants.EXCEL_ROW_ACCESS_WINDOW;
import static com.amf.report.constant.AppConstants.LOYALTY_EARN_RECORD_HEADERS;
import static com.amf.report.constant.AppConstants.LOYALTY_EARN_RECORD_NAME;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {
    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern(EXCEL_FILE_TIMESTAMP_PATTERN);

    private final Path outputDir;

    public ExcelServiceImpl(@Value("${report.output-dir}") String outputDir) {
        this.outputDir = Paths.get(outputDir);
    }

    @Override
    public Path writeLoyaltyEarnRecords(List<LoyaltyEarnRecord> records) {
        Path file = outputDir.resolve(
                LOYALTY_EARN_RECORD_NAME + "_" + LocalDateTime.now().format(FILE_TIMESTAMP) + EXCEL_FILE_EXTENSION);

        SXSSFWorkbook workbook = new SXSSFWorkbook(EXCEL_ROW_ACCESS_WINDOW);
        workbook.setCompressTempFiles(true);
        try {
            Files.createDirectories(outputDir);
            Sheet sheet = workbook.createSheet(LOYALTY_EARN_RECORD_NAME);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle decimalStyle = formatStyle(workbook, EXCEL_DECIMAL_FORMAT);
            CellStyle dateTimeStyle = formatStyle(workbook, EXCEL_DATE_TIME_FORMAT);

            writeHeader(sheet, headerStyle);
            int rowIndex = 1;
            for (LoyaltyEarnRecord record : records) {
                writeRow(sheet.createRow(rowIndex++), record, decimalStyle, dateTimeStyle);
            }

            try (OutputStream out = Files.newOutputStream(file)) {
                workbook.write(out);
            }
            log.info("Wrote {} loyaltyEarn record(s) to {}", records.size(), file.toAbsolutePath());
            return file;
        } catch (IOException ex) {
            throw new ExcelGenerationException("Failed to write Excel file " + file + ": " + ex.getMessage(), ex);
        } finally {
            closeQuietly(workbook);
        }
    }

    private static void writeHeader(Sheet sheet, CellStyle headerStyle) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < LOYALTY_EARN_RECORD_HEADERS.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(LOYALTY_EARN_RECORD_HEADERS.get(i));
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, EXCEL_COLUMN_WIDTH);
        }
        sheet.createFreezePane(0, 1);
    }

    private static void writeRow(Row row, LoyaltyEarnRecord record, CellStyle decimalStyle, CellStyle dateTimeStyle) {
        int col = 0;
        setText(row, col++, record.getId());
        setDecimal(row, col++, record.getPlanContribution(), decimalStyle);
        setText(row, col++, record.getEarningType());
        setText(row, col++, record.getPolicyName());
        setText(row, col++, record.getOrganizationName());
        setText(row, col++, record.getCidn());
        setDecimal(row, col++, record.getTotalBalance(), decimalStyle);
        setDateTime(row, col, record.getCreatedAt(), dateTimeStyle);
    }

    private static void setText(Row row, int col, String value) {
        if (value != null) {
            row.createCell(col).setCellValue(value);
        }
    }

    private static void setDecimal(Row row, int col, BigDecimal value, CellStyle style) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(style);
        }
    }

    private static void setDateTime(Row row, int col, LocalDateTime value, CellStyle style) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value);
            cell.setCellStyle(style);
        }
    }

    private static CellStyle headerStyle(SXSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private static CellStyle formatStyle(SXSSFWorkbook workbook, String format) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        return style;
    }

    private static void closeQuietly(SXSSFWorkbook workbook) {
        try {
            workbook.close();
        } catch (IOException ex) {
            log.warn("Failed to close Excel workbook", ex);
        }
    }
}
