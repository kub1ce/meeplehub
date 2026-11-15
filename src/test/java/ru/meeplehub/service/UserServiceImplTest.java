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
                "Player One"
        );

        assertNotNull(user.getId());
        assertEquals("player_1", user.getUsername());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        userService.registerUser(
                "player_1", "one@example.com", "Player One");

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(
                        "PLAYER_1", "two@example.com", "Player Two"));
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(
                        "player_1", "wrong-email", "Player One"));
    }

    @Test
    void shouldUpdateProfile() {
        User user = userService.registerUser(
                "player_1", "one@example.com", "Player One");

        User updated = userService.updateProfile(
                user.getId(), "new@example.com", "New Name");

        assertEquals("new@example.com", updated.getEmail());
        assertEquals("New Name", updated.getDisplayName());
    }

    @Test
    void shouldChangeStatus() {
        User user = userService.registerUser(
                "player_1", "one@example.com", "Player One");

        User updated = userService.changeStatus(
                user.getId(), UserStatus.BLOCKED);

        assertEquals(UserStatus.BLOCKED, updated.getStatus());
    }

    @Test
    void shouldDeleteUser() {
        User user = userService.registerUser(
                "player_1", "one@example.com", "Player One");

        userService.deleteById(user.getId());

        assertTrue(userService.findById(user.getId()).isEmpty());
    }
}