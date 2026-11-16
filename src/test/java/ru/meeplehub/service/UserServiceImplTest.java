package ru.meeplehub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.UserStatus;
import ru.meeplehub.repository.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BoardGameRepository boardGameRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        reviewRepository.deleteAll();
        eventRepository.deleteAll();
        boardGameRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUser() {
        User user = userService.registerUser(
                "player_1",
                "player@example.com",
                "Player One",
                "Password123!"
        );

        assertNotNull(user.getId());
        assertEquals("player_1", user.getUsername());
        assertEquals(UserStatus.ACTIVE, user.getStatus());

        assertNotEquals("Password123!", user.getPasswordHash());

        assertTrue(passwordEncoder.matches(
                "Password123!",
                user.getPasswordHash()
        ));
    }

    @Test
    void shouldRejectDuplicateUsername() {
        userService.registerUser(
                "player_1", "one@example.com", "Player One","Password123!");

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(
                        "PLAYER_1", "two@example.com", "Player Two","Password123!"));
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(
                        "player_1", "wrong-email", "Player One","Password123!"));
    }

    @Test
    void shouldUpdateProfile() {
        User user = userService.registerUser(
                "player_1", "one@example.com", "Player One","Password123!");

        User updated = userService.updateProfile(
                user.getId(), "new@example.com", "New Name");

        assertEquals("new@example.com", updated.getEmail());
        assertEquals("New Name", updated.getDisplayName());
    }

    @Test
    void shouldChangeStatus() {
        User user = userService.registerUser(
                "player_1", "one@example.com", "Player One","Password123!");

        User updated = userService.changeStatus(
                user.getId(), UserStatus.BLOCKED);

        assertEquals(UserStatus.BLOCKED, updated.getStatus());
    }

    @Test
    void shouldDeleteUser() {
        User user = userService.registerUser(
                "player_1", "one@example.com", "Player One","Password123!");

        userService.deleteById(user.getId());

        assertTrue(userService.findById(user.getId()).isEmpty());
    }
}