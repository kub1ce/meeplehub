package ru.meeplehub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.meeplehub.domain.entity.BoardGame;
import ru.meeplehub.domain.entity.Report;
import ru.meeplehub.domain.enums.ReportStatus;
import ru.meeplehub.repository.BoardGameRepository;
import ru.meeplehub.repository.ReportRepository;
import ru.meeplehub.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log =
            LoggerFactory.getLogger(ReportServiceImpl.class);

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;

    public ReportServiceImpl(
            ReportRepository reportRepository,
            UserRepository userRepository,
            BoardGameRepository boardGameRepository) {

        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
    }

    @Override
    public Long createReport() {
        Report report = new Report();

        report.setStatus(ReportStatus.CREATED);

        return reportRepository.save(report).getId();
    }

    @Override
    public Optional<Report> getReport(Long id) {
        return reportRepository.findById(id);
    }

    // Запускает формирование отчёта в фоновом режиме
    @Override
    public CompletableFuture<Void> generateReportAsync(Long id) {
        return CompletableFuture.runAsync(() -> generateReport(id));
    }

    private void generateReport(Long id) {
        Report report = reportRepository.findById(id).orElse(null);

        if (report == null) {
            log.error("Отчёт с id {} не найден.", id);
            return;
        }

        try {
            long totalStartTime = System.currentTimeMillis();

            AtomicLong usersCount = new AtomicLong();
            AtomicLong usersElapsed = new AtomicLong();

            AtomicReference<List<BoardGame>> boardGames =
                    new AtomicReference<>(List.of());

            AtomicLong boardGamesElapsed = new AtomicLong();

            AtomicReference<Exception> taskError =
                    new AtomicReference<>();

            // Первый поток: подсчёт пользователей.
            Thread usersThread = new Thread(() -> {
                long startTime = System.currentTimeMillis();

                try {
                    usersCount.set(userRepository.count());
                } catch (Exception exception) {
                    taskError.compareAndSet(null, exception);
                } finally {
                    usersElapsed.set(
                            System.currentTimeMillis() - startTime
                    );
                }
            }, "report-users-count");

            // Второй поток: получение списка настольных игр.
            Thread boardGamesThread = new Thread(() -> {
                long startTime = System.currentTimeMillis();

                try {
                    List<BoardGame> games = new ArrayList<>();

                    boardGameRepository.findAll().forEach(games::add);

                    boardGames.set(games);

                } catch (Exception exception) {
                    taskError.compareAndSet(null, exception);
                } finally {
                    boardGamesElapsed.set(
                            System.currentTimeMillis() - startTime
                    );
                }
            }, "report-board-games");

            // Запускаем оба потока
            usersThread.start();
            boardGamesThread.start();

            // Ожидаем завершения обоих потоков
            try {
                usersThread.join();
                boardGamesThread.join();

            } catch (InterruptedException exception) {
                usersThread.interrupt();
                boardGamesThread.interrupt();

                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Формирование отчёта было прервано.",
                        exception
                );
            }

            // Проверяем, не возникли ли ошибки в рабочих потоках
            if (taskError.get() != null) {
                throw new IllegalStateException(
                        "Ошибка при получении данных для отчёта.",
                        taskError.get()
                );
            }

            // Формируем HTML
            String content = buildReportHtml(
                    usersCount.get(),
                    boardGames.get(),
                    usersElapsed.get(),
                    boardGamesElapsed.get()
            );

            long totalElapsed =
                    System.currentTimeMillis() - totalStartTime;

            content = content.replace(
                    "{{TOTAL_TIME}}",
                    String.valueOf(totalElapsed)
            );

            report.setContent(content);
            report.setStatus(ReportStatus.COMPLETED);

        } catch (Exception exception) {
            log.error(
                    "Ошибка формирования отчёта с id {}.",
                    id,
                    exception
            );

            report.setContent(null);
            report.setStatus(ReportStatus.ERROR);
        }

        reportRepository.save(report);
    }

    // Создаёт HTML-страницу со статистикой приложения
    private String buildReportHtml(
            long usersCount,
            List<BoardGame> boardGames,
            long usersElapsed,
            long boardGamesElapsed) {

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>MeepleHub — Статистический отчёт</title>

                    <style>
                        body {
                            max-width: 1100px;
                            margin: 40px auto;
                            padding: 0 20px;
                            font-family: Arial, sans-serif;
                            color: #252525;
                            background: #f5f5f5;
                        }

                        h1, h2 {
                            color: #292929;
                        }

                        .section {
                            margin-top: 25px;
                            padding: 20px;
                            background: white;
                            border-radius: 8px;
                        }

                        .stat {
                            font-size: 18px;
                        }

                        table {
                            width: 100%;
                            margin-top: 15px;
                            border-collapse: collapse;
                            background: white;
                        }

                        th, td {
                            padding: 12px;
                            text-align: left;
                            border-bottom: 1px solid #ddd;
                        }

                        th {
                            color: white;
                            background: #292929;
                        }

                        .empty {
                            text-align: center;
                            color: #777;
                            padding: 25px;
                        }

                        .time {
                            color: #555;
                            margin-top: 12px;
                        }
                    </style>
                </head>

                <body>
                    <h1>MeepleHub — Статистический отчёт</h1>

                    <div class="section">
                        <h2>Пользователи</h2>
                """);

        html.append("<p class=\"stat\">Количество зарегистрированных пользователей: ")
                .append("<strong>")
                .append(usersCount)
                .append("</strong></p>");

        html.append("<p class=\"time\">Время подсчёта пользователей: ")
                .append(usersElapsed)
                .append(" мс</p>");

        html.append("""
                    </div>

                    <div class="section">
                        <h2>Каталог настольных игр</h2>

                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Название</th>
                                    <th>Издатель</th>
                                    <th>Категория</th>
                                    <th>Игроки</th>
                                    <th>Время</th>
                                    <th>Возраст</th>
                                </tr>
                            </thead>
                            <tbody>
                """);

        if (boardGames.isEmpty()) {
            html.append("""
                    <tr>
                        <td colspan="7" class="empty">
                            В каталоге пока нет настольных игр.
                        </td>
                    </tr>
                    """);
        } else {
            for (BoardGame game : boardGames) {
                html.append("<tr>");

                html.append("<td>")
                        .append(game.getId())
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(game.getTitle()))
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(game.getPublisher()))
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(game.getCategory()))
                        .append("</td>");

                html.append("<td>")
                        .append(game.getMinPlayers())
                        .append("–")
                        .append(game.getMaxPlayers())
                        .append("</td>");

                html.append("<td>")
                        .append(game.getDurationMinutes())
                        .append(" мин.")
                        .append("</td>");

                html.append("<td>")
                        .append(game.getMinAge())
                        .append("+")
                        .append("</td>");

                html.append("</tr>");
            }
        }

        html.append("""
                            </tbody>
                        </table>
                """);

        html.append("<p class=\"time\">Время получения списка настольных игр: ")
                .append(boardGamesElapsed)
                .append(" мс</p>");

        html.append("""
                    </div>

                    <div class="section">
                        <h2>Итоговое время</h2>

                        <p class="stat">
                            Общее время формирования отчёта:
                            <strong>{{TOTAL_TIME}} мс</strong>
                        </p>
                    </div>
                </body>
                </html>
                """);

        return html.toString();
    }

    // Экранирует HTML-символы пользовательских данных
    private String escapeHtml(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}