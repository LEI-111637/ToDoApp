package com.example.examplefeature.ui.export;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

@Service
public class CsvExportService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withZone(ZoneId.systemDefault());

    /** Converte linhas em CSV UTF-8 com BOM (para Excel abrir acentos). */
    public String tasksToCsv(List<Row> rows) {
        StringWriter sw = new StringWriter();
        sw.write('\uFEFF'); // BOM

        try (CSVWriter w = new CSVWriter(sw)) {
            w.writeNext(new String[]{"Id", "Descrição", "Data Limite", "Data Criação"});
            for (Row r : rows) {
                String due = r.dueDate() == null ? "" : DATE_FMT.format(r.dueDate());
                String created = r.creationDate() == null ? "" : DATETIME_FMT.format(r.creationDate());
                w.writeNext(new String[]{
                        String.valueOf(r.id()), r.description(), due, created
                });
            }
        } catch (IOException e) {
            // Evita propagar checked exception pela app inteira
            throw new UncheckedIOException(e);
        }
        return sw.toString();
    }

    public record Row(
            Long id,
            String description,
            java.time.LocalDate dueDate,
            java.time.temporal.TemporalAccessor creationDate
    ) {}
}