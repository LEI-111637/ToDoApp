package com.example.examplefeature.ui.export;

import com.example.examplefeature.Task;
import com.example.examplefeature.TaskRepository;
import com.example.examplefeature.ui.export.CsvExportService.Row;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class ExportController {

    private final TaskRepository repo;
    private final CsvExportService csv;

    public ExportController(TaskRepository repo, CsvExportService csv) {
        this.repo = repo;
        this.csv = csv;
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportAll() {
        List<Task> tasks = repo.findAll();

        List<Row> rows = tasks.stream()
                .map(t -> new Row(
                        t.getId(),
                        t.getDescription(),
                        t.getDueDate(),        // LocalDate (ou null)
                        t.getCreationDate()    // Instant/LocalDateTime (ou null)
                ))
                .toList();

        String csvString = csv.tasksToCsv(rows);
        byte[] bytes = csvString.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tarefas.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }
}