package com.amf.report.excel;

import com.amf.report.dto.OrganizationReportRecord;
import com.amf.report.exception.ExcelGenerationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static com.amf.report.constant.AppConstants.CIDN_COLUMN_WIDTH;
import static com.amf.report.constant.AppConstants.ORGANIZATION_NAME_COLUMN_WIDTH;
import static com.amf.report.constant.AppConstants.ORGANIZATION_REPORT_HEADERS;
import static com.amf.report.constant.AppConstants.ORGANIZATION_REPORT_SHEET;

public final class OrganizationReportExcelWriter {
    private static final String TEXT_FORMAT = "@";

    private OrganizationReportExcelWriter() {
    }

    public static byte[] write(List<OrganizationReportRecord> records) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(ORGANIZATION_REPORT_SHEET);
            writeHeader(workbook, sheet);

            DataFormat dataFormat = workbook.createDataFormat();
            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setDataFormat(dataFormat.getFormat(TEXT_FORMAT));

            int rowIndex = 1;
            for (OrganizationReportRecord record : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(nullToEmpty(record.organizationName()));
                Cell cidnCell = row.createCell(1);
                cidnCell.setCellValue(nullToEmpty(record.cidn()));
                cidnCell.setCellStyle(textStyle);
            }

            sheet.setColumnWidth(0, ORGANIZATION_NAME_COLUMN_WIDTH);
            sheet.setColumnWidth(1, CIDN_COLUMN_WIDTH);
            sheet.createFreezePane(0, 1);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new ExcelGenerationException("Failed to create organization report: " + ex.getMessage(), ex);
        }
    }

    private static void writeHeader(XSSFWorkbook workbook, Sheet sheet) {
        Font bold = workbook.createFont();
        bold.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(bold);

        Row header = sheet.createRow(0);
        for (int i = 0; i < ORGANIZATION_REPORT_HEADERS.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(ORGANIZATION_REPORT_HEADERS.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
