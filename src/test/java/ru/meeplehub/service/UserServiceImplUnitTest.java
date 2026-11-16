
package ru.meeplehub.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.meeplehub.config.AppConfiguration;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.UserRole;
import ru.meeplehub.domain.enums.UserStatus;
import ru.meeplehub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppConfiguration appConfiguration;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByUsernameIgnoreCase("player_1"))
                .thenReturn(false);

        when(userRepository.existsByEmailIgnoreCase("player@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        User result = userService.registerUser(
                " player_1 ",
                " PLAYER@EXAMPLE.COM ",
                " Player One ",
                "Password123!"
        );

        assertNotNull(result.getId());
        assertEquals("player_1", result.getUsername());
        assertEquals("player@example.com", result.getEmail());
        assertEquals("Player One", result.getDisplayName());
        assertEquals("encoded-password", result.getPasswordHash());
        assertEquals(UserRole.MEMBER, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getRegisteredAt());

        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectInvalidUsername() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(
                        "bad username",
                        "player@example.com",
                        "Player One",
                        "Password123!"
                )
        );

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectShortPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(
                        "player_1",
                        "player@example.com",
                        "Player One",
                        "123"
                )
        );

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectDuplicateUsername() {
        when(userRepository.existsByUsernameIgnoreCase("player_1"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(
                        "player_1",
                        "player@example.com",
                        "Player One",
                        "Password123!"
                )
        );

        assertEquals(
                "Пользователь с таким логином уже существует.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByUsernameIgnoreCase("player_1"))
                .thenReturn(false);

        when(userRepository.existsByEmailIgnoreCase("player@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(
                        "player_1",
                        "player@example.com",
                        "Player One",
                        "Password123!"
                )
        );

        assertEquals(
                "Пользователь с таким email уже существует.",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectProfileUpdateWithExistingEmail() {
        User user = createUser();
        user.setEmail("old@example.com");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByEmailIgnoreCase("taken@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateProfile(
                        1L,
                        "taken@example.com",
                        "New Name"
                )
        );

        assertEquals("old@example.com", user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(404L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteById(404L)
        );

        assertEquals(
                "Пользователь с id 404 не найден.",
                exception.getMessage()
        );

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void shouldChangeUserStatusSuccessfully() {
        User user = createUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        User result = userService.changeStatus(
                1L,
                UserStatus.BLOCKED
        );

        assertEquals(UserStatus.BLOCKED, result.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectNullUserStatus() {
        User user = createUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.changeStatus(1L, null)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    private User createUser() {
        User user = new User();

        user.setId(1L);
        user.setUsername("player_1");
        user.setEmail("player@example.com");
        user.setDisplayName("Player One");
        user.setPasswordHash("encoded-password");
        user.setRegisteredAt(LocalDateTime.now());
        user.setRole(UserRole.MEMBER);
        user.setStatus(UserStatus.ACTIVE);

        return user;
    }
}
