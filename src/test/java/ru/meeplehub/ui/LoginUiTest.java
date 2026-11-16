
package ru.meeplehub.ui;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.UserRole;
import ru.meeplehub.domain.enums.UserStatus;
import ru.meeplehub.repository.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoginUiTest {

    private static final String USERNAME = "selenium_user";
    private static final String PASSWORD = "SeleniumPassword123!";

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

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestUser();

        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless");

        driver = new FirefoxDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldLoginAndLogoutSuccessfully() {
        WebDriverWait wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(15)
        );

        driver.get("http://localhost:" + port + "/login");

        assertEquals(
                "Вход в MeepleHub",
                driver.findElement(By.tagName("h1")).getText()
        );

        // Вводим логин и пароль
        driver.findElement(By.name("username"))
                .sendKeys(USERNAME);

        driver.findElement(By.name("password"))
                .sendKeys(PASSWORD);

        // Выполняем вход.
        driver.findElement(
                By.cssSelector("button[type='submit']")
        ).click();

        // Проверяем переход в каталог
        wait.until(
                ExpectedConditions.urlContains("/games")
        );

        assertEquals(
                "MeepleHub",
                driver.findElement(By.tagName("h1")).getText()
        );

        // Нажимаем кнопку выхода
        WebElement logoutButton = driver.findElement(
                By.cssSelector(
                        "form[action='/logout'] button[type='submit']"
                )
        );

        logoutButton.click();

        // Проверяем перенаправление на страницу входа
        wait.until(
                ExpectedConditions.urlContains("/login?logout")
        );

        assertEquals(
                "Вход в MeepleHub",
                driver.findElement(By.tagName("h1")).getText()
        );

        String message = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".message.success")
                )
        ).getText();

        assertTrue(
                message.contains("Вы успешно вышли из системы.")
        );
    }

    private void createTestUser() {
        User user = new User();

        user.setUsername(USERNAME);
        user.setEmail("selenium@example.com");
        user.setDisplayName("Selenium Test User");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRegisteredAt(LocalDateTime.now());
        user.setRole(UserRole.MEMBER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);
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
