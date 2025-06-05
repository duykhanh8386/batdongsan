package com.BTL.Springboot.util;

import com.BTL.Springboot.entity.Project;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporterUtil {
    public static void export(HttpServletResponse response, List<Project> projects) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=projects.pdf");

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("DANH SÁCH CĂN HỘ", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(10f);
        table.setWidths(new float[]{3.5f, 3.5f, 4f, 2.5f, 2.5f, 2.5f, 3f, 3f, 2.5f});

        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

        // Header
        table.addCell(new Phrase("Tên dự án", headFont));
        table.addCell(new Phrase("Nhà phát triển", headFont));
        table.addCell(new Phrase("Địa chỉ", headFont));
        table.addCell(new Phrase("Quận/Huyện", headFont));
        table.addCell(new Phrase("Thành phố", headFont));
        table.addCell(new Phrase("Diện tích", headFont));
        table.addCell(new Phrase("Ngày bắt đầu", headFont));
        table.addCell(new Phrase("Ngày hoàn thành", headFont));
        table.addCell(new Phrase("Trạng thái", headFont));

        // Format ngày
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (Project apt : projects) {
            table.addCell(apt.getProjectName());
            table.addCell(apt.getDeveloper());
            table.addCell(apt.getLocation());
            table.addCell(apt.getState());
            table.addCell(apt.getCity());
            table.addCell(String.format("%.2f", apt.getTotalArea()));
            table.addCell(apt.getStartDate() != null ? apt.getStartDate().format(dateFormatter) : "");
            table.addCell(apt.getCompletionDate() != null ? apt.getCompletionDate().format(dateFormatter) : "");
            table.addCell(apt.getStatus());
        }

        document.add(table);
        document.close();
    }
}
