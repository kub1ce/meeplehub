package ru.meeplehub.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.meeplehub.domain.entity.Report;
import ru.meeplehub.domain.enums.ReportStatus;
import ru.meeplehub.service.ReportService;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Создаёт запись отчёта и запускает его формирование.
     */
    @PostMapping
    public ResponseEntity<Long> createReport() {

        Long reportId = reportService.createReport();

        // Запускаем фоновую задачу и не ожидаем её завершения.
        reportService.generateReportAsync(reportId);

        return ResponseEntity.accepted()
                .location(URI.create("/api/reports/" + reportId))
                .body(reportId);
    }

    /**
     * Возвращает HTML-содержимое отчёта или информацию о его состоянии.
     */
    @GetMapping(value = "/{id}", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> getReport(@PathVariable Long id) {

        Optional<Report> optionalReport = reportService.getReport(id);

        if (optionalReport.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Report report = optionalReport.get();

        if (report.getStatus() == ReportStatus.CREATED) {
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .contentType(MediaType.TEXT_HTML)
                    .body(statusPage(
                            "Отчёт ещё формируется. Попробуйте обновить страницу позже."
                    ));
        }

        if (report.getStatus() == ReportStatus.ERROR) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_HTML)
                    .body(statusPage(
                            "При формировании отчёта произошла ошибка."
                    ));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(report.getContent());
    }

    private String statusPage(String message) {
        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <title>MeepleHub — Отчёт</title>
                </head>
                <body>
                    <h1>MeepleHub — Статус отчёта</h1>
                    <p>%s</p>
                </body>
                </html>
                """.formatted(message);
    }
}