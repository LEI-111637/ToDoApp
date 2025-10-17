package com.example.pdf;

import com.example.examplefeature.Task;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] exportTasksToPdf(List<Task> tasks) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin;

            // título
            cs.beginText();
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            cs.newLineAtOffset(margin, y);
            cs.showText("ToDoApp - Lista de Tarefas");
            cs.endText();
            y -= 30;

            // cabeçalhos
            cs.beginText();
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            cs.newLineAtOffset(margin, y);
            cs.showText("Descrição                              | Criada em              | Prazo");
            cs.endText();
            y -= 18;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Task t : tasks) {
                if (y < 70) { // nova página se faltar espaço
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                    y = page.getMediaBox().getHeight() - margin;
                }

                String descricao = truncate(t.getDescription(), 34);
                String criacao = t.getCreationDate() != null ? t.getCreationDate().toString() : "";
                String prazo = t.getDueDate() != null ? fmt.format(t.getDueDate()) : "-";

                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format("%-34s | %-20s | %s", descricao, criacao, prazo));
                cs.endText();
                y -= 16;
            }

            cs.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }
}
