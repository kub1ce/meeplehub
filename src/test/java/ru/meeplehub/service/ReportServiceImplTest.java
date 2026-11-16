package ru.meeplehub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.meeplehub.domain.entity.BoardGame;
import ru.meeplehub.domain.entity.Report;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.ReportStatus;
import ru.meeplehub.domain.enums.UserRole;
import ru.meeplehub.domain.enums.UserStatus;
import ru.meeplehub.repository.*;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ReportServiceImplTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardGameRepository boardGameRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        reviewRepository.deleteAll();
        eventRepository.deleteAll();

        boardGameRepository.deleteAll();
        userRepository.deleteAll();

        reportRepository.deleteAll();
    }

    @Test
    void shouldCreateReportWithCreatedStatus() {

        Long id = reportService.createReport();

        Report report = reportRepository.findById(id)
                .orElseThrow();

        assertNotNull(id);
        assertEquals(ReportStatus.CREATED, report.getStatus());
        assertNull(report.getContent());
    }

    @Test
    void shouldGenerateHtmlReportAsynchronously() throws Exception {

        User user = new User();

        user.setUsername("report_user");
        user.setEmail("report@example.com");
        user.setDisplayName("Report User");
        user.setRegisteredAt(LocalDateTime.now());
        user.setRole(UserRole.MEMBER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        BoardGame game = new BoardGame();

        game.setTitle("Тестовая игра");
        game.setDescription("Игра для проверки отчёта");
        game.setMinPlayers(2);
        game.setMaxPlayers(4);
        game.setDurationMinutes(60);
        game.setMinAge(10);
        game.setPublisher("MeepleHub");
        game.setCategory("Стратегия");

        boardGameRepository.save(game);

        Long id = reportService.createReport();

        reportService.generateReportAsync(id)
                .get(10, TimeUnit.SECONDS);

        Report report = reportRepository.findById(id)
                .orElseThrow();

        assertEquals(ReportStatus.COMPLETED, report.getStatus());

        assertNotNull(report.getContent());

        assertTrue(report.getContent().contains(
                "Количество зарегистрированных пользователей"
        ));

        assertTrue(report.getContent().contains(
                "Тестовая игра"
        ));

        assertTrue(report.getContent().contains(
                "Время подсчёта пользователей"
        ));

        assertTrue(report.getContent().contains(
                "Время получения списка настольных игр"
        ));

        assertTrue(report.getContent().contains(
                "Общее время формирования отчёта"
        ));

        assertFalse(report.getContent().contains("{{TOTAL_TIME}}"));
    }
}