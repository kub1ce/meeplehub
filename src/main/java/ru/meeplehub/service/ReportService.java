package ru.meeplehub.service;

import ru.meeplehub.domain.entity.Report;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ReportService {

    Long createReport();

    Optional<Report> getReport(Long id);

    CompletableFuture<Void> generateReportAsync(Long id);
}