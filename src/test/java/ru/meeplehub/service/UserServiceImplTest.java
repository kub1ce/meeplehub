package ru.meeplehub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.meeplehub.config.AppConfiguration;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.UserStatus;
import ru.meeplehub.repository.UserRepository;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceImplTest {

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        UserRepository repository = new UserRepository(new ArrayList<>());
        AppConfiguration appConfiguration = new AppConfiguration("MeepleHub test", "test");
        userService = new UserServiceImpl(repository, appConfiguration);
    }

    @Test
    void shouldRegisterUserWithGeneratedIdAndActiveStatus() {
        User user = userService.registerUser("player_1", "player@example.com", "Player One");

        assertEquals(1L, user.getId());
        assertEquals("player_1", user.getUsername());
        assertEquals("player@example.com", user.getEmail());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(user.getRegisteredAt() != null);
    }

    @Test
    void shouldRejectDuplicateUsernameIgnoringCase() {
        userService.registerUser("player_1", "one@example.com", "Player One");

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("PLAYER_1", "two@example.com", "Player Two"));
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser("player_1", "not-an-email", "Player One"));
    }

    @Test
    void shouldUpdateProfile() {
        User created = userService.registerUser("player_1", "one@example.com", "Player One");

        User updated = userService.updateProfile(created.getId(), "new@example.com", "New Name");

        assertEquals("new@example.com", updated.getEmail());
        assertEquals("New Name", updated.getDisplayName());
    }

    @Test
    void shouldChangeStatus() {
        User created = userService.registerUser("player_1", "one@example.com", "Player One");

        User updated = userService.changeStatus(created.getId(), UserStatus.BLOCKED);

        assertEquals(UserStatus.BLOCKED, updated.getStatus());
    }

    @Test
    void shouldDeleteUser() {
        User created = userService.registerUser("player_1", "one@example.com", "Player One");

        userService.deleteById(created.getId());

        assertFalse(userService.findById(created.getId()).isPresent());
    }
}
