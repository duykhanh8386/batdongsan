package com.BTL.Springboot.util;

import com.BTL.Springboot.entity.Project;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

public class ExcelExporterUtil {
    List<Project> projects;

    public ExcelExporterUtil(List<Project> projects) {
        this.projects = projects;
    }

    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=apartments.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Apartments");
        CreationHelper createHelper = workbook.getCreationHelper();

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);


        // Date style
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);
        dateStyle.setAlignment(HorizontalAlignment.CENTER);
        dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Decimal style
        CellStyle decimalStyle = workbook.createCellStyle();
        decimalStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        decimalStyle.setBorderTop(BorderStyle.THIN);
        decimalStyle.setBorderBottom(BorderStyle.THIN);
        decimalStyle.setBorderLeft(BorderStyle.THIN);
        decimalStyle.setBorderRight(BorderStyle.THIN);

        // Text cell style with borders
        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setBorderTop(BorderStyle.THIN);
        textStyle.setBorderBottom(BorderStyle.THIN);
        textStyle.setBorderLeft(BorderStyle.THIN);
        textStyle.setBorderRight(BorderStyle.THIN);
        textStyle.setAlignment(HorizontalAlignment.LEFT);
        textStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Header row
        String[] headers = {
                "Tên dự án","Nhà phát triển", "Địa chỉ",
                "Quận/Huyện", "Thành phố", "Diện tích","Đơn giá",
                "Ngày bắt đầu", "Ngày hoàn thành"
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowCount = 1;
        for (Project apt : projects) {
            Row row = sheet.createRow(rowCount++);

            row.createCell(0).setCellValue(apt.getProjectName());
            row.getCell(0).setCellStyle(textStyle);

            row.createCell(1).setCellValue(apt.getDeveloper());
            row.getCell(1).setCellStyle(textStyle);

            row.createCell(2).setCellValue(apt.getLocation());
            row.getCell(2).setCellStyle(textStyle);

            row.createCell(3).setCellValue(apt.getState());
            row.getCell(3).setCellStyle(textStyle);

            row.createCell(4).setCellValue(apt.getCity());
            row.getCell(4).setCellStyle(textStyle);

            Cell areaCell = row.createCell(5);
            areaCell.setCellValue(apt.getTotalArea());
            areaCell.setCellStyle(decimalStyle);

            Cell totalCell = row.createCell(6);
            totalCell.setCellValue(apt.getTotalUnits());
            totalCell.setCellStyle(decimalStyle);

            Cell startDateCell = row.createCell(7);
            if (apt.getStartDate() != null)
                startDateCell.setCellValue(apt.getStartDate());
            startDateCell.setCellStyle(dateStyle);

            Cell endDateCell = row.createCell(8);
            if (apt.getCompletionDate() != null)
                endDateCell.setCellValue(apt.getCompletionDate());
            endDateCell.setCellStyle(dateStyle);

        }

        // Auto resize columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
