package com.BTL.Springboot.util;

import com.BTL.Springboot.dto.response.property.PropertyDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PropertyPDFExportUtil {
    public static void export(HttpServletResponse response, List<PropertyDto> propertyDtoList) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=properties.pdf");

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = getArialBoldFont(16f);
        Paragraph title = new Paragraph("DANH SÁCH BẤT ĐỘNG SẢN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Sử dụng bảng 16 cột
        PdfPTable table = new PdfPTable(13);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(10f);

        // Optional: điều chỉnh tỷ lệ cột nếu muốn
        table.setWidths(new float[]{
                2.5f, 2.5f, 3f, 3.5f,
                2.5f, 2.5f, 2f, 1.5f, 1.6f,
                1.6f, 1.5f, 2.5f, 2.5f
        });

        Font headFont = getArialBoldFont(12f);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Header
        table.addCell(new Phrase("Loại BĐS", headFont));
        table.addCell(new Phrase("Tên dự án", headFont));
        table.addCell(new Phrase("Tiêu đề", headFont));
        table.addCell(new Phrase("Mô tả", headFont));
        table.addCell(new Phrase("Địa chỉ", headFont));
        table.addCell(new Phrase("Quận/Huyện", headFont));
        table.addCell(new Phrase("Thành phố", headFont));
        table.addCell(new Phrase("Diện tích", headFont));
        table.addCell(new Phrase("Phòng ngủ", headFont));
        table.addCell(new Phrase("Phòng tắm", headFont));
        table.addCell(new Phrase("Tầng", headFont));
        table.addCell(new Phrase("Chủ sở hữu", headFont));
        table.addCell(new Phrase("Nhân viên", headFont));

        // Dữ liệu
        for (PropertyDto p : propertyDtoList) {
            table.addCell(p.getPropertyType() != null ? p.getPropertyType().getTypeName() : "");
            table.addCell(p.getProject().getProjectName());
            table.addCell(p.getTitle());
            table.addCell(p.getDescription());
            table.addCell(p.getAddress());
            table.addCell(p.getState());
            table.addCell(p.getCity());
            table.addCell(p.getArea() != null ? String.format("%.2f", p.getArea()) : "");
            table.addCell(p.getBedrooms() != null ? p.getBedrooms().toString() : "");
            table.addCell(p.getBathrooms() != null ? p.getBathrooms().toString() : "");
            table.addCell(p.getFloors() != null ? p.getFloors().toString() : "");
            table.addCell(
                    p.getOwner() != null ? p.getOwner().getFirstName() + " " + p.getOwner().getLastName() : "");
            table.addCell(
                    p.getListingAgent() != null ? p.getListingAgent().getFirstName() + " " + p.getListingAgent().getLastName() : "");
        }

        document.add(table);
        document.close();
    }

    private static Font getArialFont(float size) {
        try {
            InputStream fontStream = PropertyPDFExportUtil.class.getResourceAsStream("/fonts/arial.ttf");
            BaseFont baseFont = BaseFont.createFont("arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, fontStream.readAllBytes(), null);
            return new Font(baseFont, size);
        } catch (Exception e) {
            throw new RuntimeException("Could not load Arial font", e);
        }
    }

    private static Font getArialBoldFont(float size) {
        try {
            InputStream fontStream = PropertyPDFExportUtil.class.getResourceAsStream("/fonts/arialbd.ttf");
            BaseFont baseFont = BaseFont.createFont("arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, fontStream.readAllBytes(), null);
            return new Font(baseFont, size, Font.BOLD);
        } catch (Exception e) {
            throw new RuntimeException("Could not load Arial Bold font", e);
        }
    }
}
