
package ru.meeplehub.controller;

import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.response.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import ru.meeplehub.domain.entity.BoardGame;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.UserRole;
import ru.meeplehub.domain.enums.UserStatus;
import ru.meeplehub.repository.*;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReportControllerRestAssuredTest {

    private static final String USERNAME = "api_test_user";
    private static final String PASSWORD = "ApiPassword123!";

    private static final Pattern CSRF_PATTERN = Pattern.compile(
            "name=\"_csrf\"[^>]*value=\"([^\"]+)\""
    );

    @LocalServerPort
    private int port;

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

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CookieFilter cookieFilter;
    private String csrfToken;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        cookieFilter = new CookieFilter();

        cleanDatabase();
        createTestUser();
        createTestGame();

        authenticate();
    }

    @AfterEach
    void tearDown() {
        RestAssured.reset();
    }

    @Test
    void shouldCreateAndRetrieveReport() throws InterruptedException {
        Response createResponse = given()
                .filter(cookieFilter)
                .header("X-CSRF-TOKEN", csrfToken)
                .when()
                .post("/api/reports");

        assertEquals(
                HttpStatus.ACCEPTED.value(),
                createResponse.statusCode(),
                createResponse.asString()
        );

        Long reportId = createResponse.as(Long.class);

        assertNotNull(reportId);
        assertTrue(
                createResponse.header("Location")
                        .endsWith("/api/reports/" + reportId)
        );

        Response reportResponse = waitForReport(reportId);

        assertNotNull(reportResponse);

        assertEquals(
                HttpStatus.OK.value(),
                reportResponse.statusCode(),
                reportResponse.asString()
        );

        assertTrue(
                reportResponse.contentType().contains("text/html")
        );

        String html = reportResponse.asString();

        assertTrue(html.contains("Количество зарегистрированных пользователей"));
        assertTrue(html.contains("API Test Game"));
        assertTrue(html.contains("Время подсчёта пользователей"));
        assertTrue(html.contains("Время получения списка настольных игр"));
        assertTrue(html.contains("Общее время формирования отчёта"));
    }

    @Test
    void shouldReturn404ForUnknownReport() {
        given()
                .filter(cookieFilter)
                .when()
                .get("/api/reports/999999999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private Response waitForReport(Long reportId)
            throws InterruptedException {

        Response response = null;

        for (int attempt = 0; attempt < 50; attempt++) {
            response = given()
                    .filter(cookieFilter)
                    .when()
                    .get("/api/reports/" + reportId);

            if (response.statusCode() != HttpStatus.ACCEPTED.value()) {
                return response;
            }

            Thread.sleep(100);
        }

        return response;
    }

    private void authenticate() {
        Response loginPage = given()
                .filter(cookieFilter)
                .when()
                .get("/login");

        assertEquals(200, loginPage.statusCode());

        String loginCsrf = extractCsrf(loginPage.asString());

        Response loginResponse = given()
                .filter(cookieFilter)
                .redirects()
                .follow(false)
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", USERNAME)
                .formParam("password", PASSWORD)
                .formParam("_csrf", loginCsrf)
                .when()
                .post("/login");

        assertEquals(
                HttpStatus.FOUND.value(),
                loginResponse.statusCode()
        );

        // После входа Spring Security обновляет CSRF-токен.
        Response gamesPage = given()
                .filter(cookieFilter)
                .when()
                .get("/games");

        assertEquals(200, gamesPage.statusCode());
        assertTrue(gamesPage.asString().contains("<h1>MeepleHub</h1>"));

        csrfToken = extractCsrf(gamesPage.asString());
    }

    private String extractCsrf(String html) {
        Matcher matcher = CSRF_PATTERN.matcher(html);

        assertTrue(
                matcher.find(),
                "CSRF-токен не найден в HTML."
        );

        return matcher.group(1);
    }

    private void createTestUser() {
        User user = new User();

        user.setUsername(USERNAME);
        user.setEmail("api-test@example.com");
        user.setDisplayName("API Test User");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRegisteredAt(LocalDateTime.now());
        user.setRole(UserRole.MEMBER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);
    }

    private void createTestGame() {
        BoardGame game = new BoardGame();

        game.setTitle("API Test Game");
        game.setDescription("Game used by RestAssured tests");
        game.setMinPlayers(2);
        game.setMaxPlayers(4);
        game.setDurationMinutes(30);
        game.setMinAge(8);
        game.setPublisher("MeepleHub");
        game.setCategory("Test");

        boardGameRepository.save(game);
    }

    private void cleanDatabase() {
        registrationRepository.deleteAll();
        reviewRepository.deleteAll();
        eventRepository.deleteAll();
        boardGameRepository.deleteAll();
        reportRepository.deleteAll();
        userRepository.deleteAll();
    }
}
