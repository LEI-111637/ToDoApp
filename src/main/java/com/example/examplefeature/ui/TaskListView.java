package com.example.examplefeature.ui;


import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.MultiFileReceiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.UI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
class TaskListView extends Main {

    private final TaskService taskService;

    final TextField description;
    final DatePicker dueDate;
    final Button createBtn;
    Button exportBtn = new Button("Exportar PDF", e -> UI.getCurrent().getPage().open("/api/tasks/export"));
    final Grid<Task> taskGrid;
    final Anchor exportAnchor;
    final Upload qrUpload; // novo upload de QR codes

    TaskListView(TaskService taskService) {
        this.taskService = taskService;

        // Campo descrição
        // Campo de descrição da tarefa
        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        // Campo data limite
        // Campo de data
        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");

        // Botão criar tarefa
        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Botão exportar CSV (ligado ao endpoint REST)
        exportAnchor = new Anchor("/api/tasks/export.csv", "");
        exportAnchor.getElement().setAttribute("download", true);
        Button exportBtn = new Button("Export CSV");
        exportBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        exportAnchor.add(exportBtn);

        // Formatadores de data
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(getLocale());
        // Botão criar
        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Novo componente: upload de QR code
        File tempFile = new File("uploaded-qr.png");
        MultiFileReceiver receiver = (fileName, mimeType) -> {
            try {
                return new FileOutputStream(tempFile);
            } catch (Exception e) {
                Notification.show("Erro ao criar ficheiro temporário.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return OutputStream.nullOutputStream();
            }
        };

        qrUpload = new Upload(receiver);
        qrUpload.setAcceptedFileTypes("image/png", "image/jpeg");
        qrUpload.setMaxFiles(1);
        qrUpload.addSucceededListener(event -> {
            String qrText = readQRCode(tempFile);
            if (qrText != null && !qrText.isBlank()) {
                description.setValue(qrText);
                Notification.show("QR Code lido com sucesso!", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Nenhum QR Code encontrado na imagem.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Formatação de datas
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getLocale()).withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        // Grelha de tarefas
        taskGrid = new Grid<>();
        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.addColumn(Task::getDescription).setHeader("Description");
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate())
                        .map(dateFormatter::format)
                        .orElse("Never"))
                .setHeader("Due Date");
                .map(dateFormatter::format).orElse("Never")).setHeader("Due Date");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate()))
                .setHeader("Creation Date");
        taskGrid.setSizeFull();

        // Layout principal
        setSizeFull();
        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.SMALL
        );

        // Toolbar com os botões
        add(new ViewToolbar("Task List",
                ViewToolbar.group(description, dueDate, createBtn, exportAnchor))
        );
        // Layout e estilo
        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        // Toolbar superior (agora com upload QR)
        add(new ViewToolbar("Task List",
                ViewToolbar.group(description, dueDate, createBtn, qrUpload)));

        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn,exportBtn)));
        add(taskGrid);
    }

    private void createTask() {
        taskService.createTask(description.getValue(), dueDate.getValue());
        taskGrid.getDataProvider().refreshAll();
        description.clear();
        dueDate.clear();
        Notification.show("Task added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}

    /**
     * Método auxiliar para ler o texto de um QR Code usando ZXing.
     */
    private String readQRCode(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) return null;
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
