package com.BTL.Springboot.util;

import com.BTL.Springboot.entity.Project;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import jakarta.servlet.http.HttpServletResponse;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporterUtil {

    public static void export(HttpServletResponse response, List<Project> projects) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=projects.pdf");

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        InputStream fontStream = PDFExporterUtil.class.getResourceAsStream("/fonts/DejaVuSans.ttf");

        BaseFont baseFont = BaseFont.createFont(
                "DejaVuSans.ttf",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                BaseFont.CACHED,
                fontStream.readAllBytes(),
                null
        );

        Font fontNormal = new Font(baseFont, 11, Font.NORMAL);
        Font fontBold = new Font(baseFont, 12, Font.BOLD);

        Paragraph title = new Paragraph("DANH SÁCH CĂN HỘ", fontBold);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(10f);
        table.setWidths(new float[]{5f, 4f, 6f, 3f, 3f, 3f, 3f, 3.5f, 3.5f});

        addTableHeader(table, fontBold);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (Project apt : projects) {
            table.addCell(createCell(apt.getProjectName(), fontNormal, true));   // cột dài → cho phép xuống dòng
            table.addCell(createCell(apt.getDeveloper(), fontNormal, true));
            table.addCell(createCell(apt.getLocation(), fontNormal, true));
            table.addCell(createCell(apt.getState(), fontNormal, false));
            table.addCell(createCell(apt.getCity(), fontNormal, false));
            table.addCell(createCell(String.format("%.2f", apt.getTotalArea()), fontNormal, false));
            table.addCell(createCell(String.format("%d", apt.getTotalUnits()), fontNormal, false));
            table.addCell(createCell(
                    apt.getStartDate() != null ? apt.getStartDate().format(formatter) : "", fontNormal, false));
            table.addCell(createCell(
                    apt.getCompletionDate() != null ? apt.getCompletionDate().format(formatter) : "", fontNormal, false));
        }

        document.add(table);
        document.close();
    }

    private static PdfPCell createCell(String text, Font font, boolean allowWrap) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (!allowWrap) {
            cell.setNoWrap(true);
        }
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setMinimumHeight(20f);
        return cell;
    }

    private static void addTableHeader(PdfPTable table, Font font) {
        table.addCell(createHeaderCell("Tên dự án", font));
        table.addCell(createHeaderCell("Nhà phát triển", font));
        table.addCell(createHeaderCell("Địa chỉ", font));
        table.addCell(createHeaderCell("Quận/Huyện", font));
        table.addCell(createHeaderCell("Thành phố", font));
        table.addCell(createHeaderCell("Diện tích", font));
        table.addCell(createHeaderCell("Đơn giá", font));
        table.addCell(createHeaderCell("Ngày bắt đầu", font));
        table.addCell(createHeaderCell("Ngày hoàn thành", font));
    }

    private static PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setMinimumHeight(20f);
        return cell;
    }
}