package ru.meeplehub.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.meeplehub.config.AppConfiguration;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.UserRole;
import ru.meeplehub.domain.enums.UserStatus;
import ru.meeplehub.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AppConfiguration appConfiguration;
    private final AtomicLong idSequence;

    public UserServiceImpl(UserRepository userRepository, AppConfiguration appConfiguration) {
        this.userRepository = userRepository;
        this.appConfiguration = appConfiguration;
        long nextId = userRepository.readAll().stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1L;
        this.idSequence = new AtomicLong(nextId);
    }

    /** Выводит свойства приложения после полной инициализации сервисного бина. */
    @PostConstruct
    public void printApplicationInfo() {
        System.out.printf("%s v%s готов к работе.%n",
                appConfiguration.getAppName(), appConfiguration.getAppVersion());
    }

    @Override
    public User registerUser(String username, String email, String displayName) {
        String normalizedUsername = requireText(username, "Логин", 3, 30);
        if (!normalizedUsername.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("Логин может содержать латинские буквы, цифры, точку, дефис и подчёркивание.");
        }

        String normalizedEmail = requireText(email, "Email", 5, 254).toLowerCase(Locale.ROOT);
        if (!normalizedEmail.matches("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")) {
            throw new IllegalArgumentException("Указан некорректный email.");
        }

        String normalizedDisplayName = requireText(displayName, "Отображаемое имя", 2, 80);
        ensureUsernameAndEmailAreUnique(normalizedUsername, normalizedEmail, null);

        User user = new User(
                idSequence.getAndIncrement(),
                normalizedUsername,
                normalizedEmail,
                normalizedDisplayName,
                LocalDateTime.now(),
                UserRole.MEMBER,
                UserStatus.ACTIVE
        );
        return userRepository.create(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.read(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.readAll();
    }

    @Override
    public User updateProfile(Long id, String email, String displayName) {
        User existingUser = findRequiredUser(id);
        String normalizedEmail = requireText(email, "Email", 5, 254).toLowerCase(Locale.ROOT);
        if (!normalizedEmail.matches("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")) {
            throw new IllegalArgumentException("Указан некорректный email.");
        }
        String normalizedDisplayName = requireText(displayName, "Отображаемое имя", 2, 80);
        ensureUsernameAndEmailAreUnique(existingUser.getUsername(), normalizedEmail, existingUser.getId());

        existingUser.setEmail(normalizedEmail);
        existingUser.setDisplayName(normalizedDisplayName);
        return userRepository.update(existingUser);
    }

    @Override
    public User changeStatus(Long id, UserStatus status) {
        User existingUser = findRequiredUser(id);
        if (status == null) {
            throw new IllegalArgumentException("Статус не может быть пустым.");
        }
        existingUser.setStatus(status);
        return userRepository.update(existingUser);
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.delete(id)) {
            throw new IllegalArgumentException("Пользователь с id " + id + " не найден.");
        }
    }

    private User findRequiredUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id пользователя не может быть пустым.");
        }
        return userRepository.read(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id " + id + " не найден."));
    }

    private void ensureUsernameAndEmailAreUnique(String username, String email, Long ignoredId) {
        boolean usernameExists = userRepository.readAll().stream()
                .anyMatch(user -> !Objects.equals(user.getId(), ignoredId)
                        && user.getUsername().equalsIgnoreCase(username));
        if (usernameExists) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует.");
        }

        boolean emailExists = userRepository.readAll().stream()
                .anyMatch(user -> !Objects.equals(user.getId(), ignoredId)
                        && user.getEmail().equalsIgnoreCase(email));
        if (emailExists) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует.");
        }
    }

    private String requireText(String value, String fieldName, int minLength, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым.");
        }
        String normalized = value.trim();
        if (normalized.length() < minLength || normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " должен содержать от " + minLength
                    + " до " + maxLength + " символов.");
        }
        return normalized;
    }
}
