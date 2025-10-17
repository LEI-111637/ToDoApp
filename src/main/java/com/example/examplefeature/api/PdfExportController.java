package com.example.examplefeature.api;

import com.example.examplefeature.TaskService;
import com.example.pdf.PdfExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;

@RestController
public class PdfExportController {

    private final TaskService taskService;
    private final PdfExportService pdfExportService;

    public PdfExportController(TaskService taskService, PdfExportService pdfExportService) {
        this.taskService = taskService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping(value = "/api/tasks/export", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportTasksPdf() throws IOException {
        // Puxa até 1000 tarefas (ajusta se precisares)
        var tasks = taskService.list(PageRequest.of(0, 1000));

        byte[] pdf = pdfExportService.exportTasksToPdf(tasks);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
