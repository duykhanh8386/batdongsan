package com.BTL.Springboot.util;

import com.BTL.Springboot.dto.response.property.PropertyDto;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

public class PropertyExcelExportUtil {
    private final List<PropertyDto> properties;

    public PropertyExcelExportUtil(List<PropertyDto> properties) {
        this.properties = properties;
    }

    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=properties.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Properties");
        CreationHelper createHelper = workbook.getCreationHelper();

        // Styles
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

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);
        dateStyle.setAlignment(HorizontalAlignment.CENTER);
        dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle decimalStyle = workbook.createCellStyle();
        decimalStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        decimalStyle.setBorderTop(BorderStyle.THIN);
        decimalStyle.setBorderBottom(BorderStyle.THIN);
        decimalStyle.setBorderLeft(BorderStyle.THIN);
        decimalStyle.setBorderRight(BorderStyle.THIN);
        decimalStyle.setAlignment(HorizontalAlignment.LEFT);
        decimalStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setBorderTop(BorderStyle.THIN);
        textStyle.setBorderBottom(BorderStyle.THIN);
        textStyle.setBorderLeft(BorderStyle.THIN);
        textStyle.setBorderRight(BorderStyle.THIN);
        textStyle.setAlignment(HorizontalAlignment.LEFT);
        textStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Header row
        String[] headers = {
                "Mã bất động sản", "Loại bất động sản", "Loại dự án", "Tiêu đề",
                "Mô tả", "Địa chỉ", "Quận/Huyện", "Tỉnh/Thành phố", "Diện tích",
                "Phòng ngủ", "Phòng tắm", "Tầng", "Năm xây dựng", "Nội thất",
                "Loại niêm yết", "Trạng thái", "Chủ sở hữu", "Nhân viên",
                "Ngày bắt đầu", "Ngày hoàn thành"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowCount = 1;
        for (PropertyDto p : properties) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            row.createCell(col).setCellValue(p.getPropertyCode());
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getPropertyType().getTypeName());
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(
                    p.getProject() != null ? p.getProject().getProjectName() : "");
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getTitle());
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getDescription());
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getAddress());
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getCity());
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getState());
            row.getCell(col++).setCellStyle(textStyle);

            Cell areaCell = row.createCell(col++);
            areaCell.setCellValue(p.getArea());
            areaCell.setCellStyle(decimalStyle);

            row.createCell(col).setCellValue(p.getBedrooms() != null ? p.getBedrooms() : 0);
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getBathrooms() != null ? p.getBathrooms() : 0);
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getFloors() != null ? p.getFloors() : 0);
            row.getCell(col++).setCellStyle(textStyle);

            Cell yearBuiltCell = row.createCell(col++);
            if (p.getYearBuilt() != null) {
                yearBuiltCell.setCellValue(p.getYearBuilt());
            }
            yearBuiltCell.setCellStyle(dateStyle);

            row.createCell(col).setCellValue(p.getIsFurnished() ? "Có" : "Không");
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getListingType());
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getStatus());
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(
                    p.getOwner() != null ? p.getOwner().getFirstName() + " " + p.getOwner().getLastName() : "");
            row.getCell(col++).setCellStyle(textStyle);

            row.createCell(col).setCellValue(p.getListingAgent() != null ? p.getListingAgent().getFirstName() + " " + p.getListingAgent().getLastName() : "");
            row.getCell(col++).setCellStyle(textStyle);

            Cell startDateCell = row.createCell(col++);
            if (p.getCreatedAt() != null) {
                startDateCell.setCellValue(java.sql.Timestamp.valueOf(p.getCreatedAt()));
            }
            startDateCell.setCellStyle(dateStyle);

            Cell endDateCell = row.createCell(col++);
            if (p.getUpdatedAt() != null) {
                endDateCell.setCellValue(java.sql.Timestamp.valueOf(p.getUpdatedAt()));
            }
            endDateCell.setCellStyle(dateStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
