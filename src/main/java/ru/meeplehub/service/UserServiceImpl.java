package ru.meeplehub.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.meeplehub.config.AppConfiguration;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.UserRole;
import ru.meeplehub.domain.enums.UserStatus;
import ru.meeplehub.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AppConfiguration appConfiguration;

    public UserServiceImpl(
            UserRepository userRepository,
            AppConfiguration appConfiguration) {
        this.userRepository = userRepository;
        this.appConfiguration = appConfiguration;
    }

    @PostConstruct
    public void printApplicationInfo() {
        System.out.printf("%s v%s готов к работе.%n",
                appConfiguration.getAppName(),
                appConfiguration.getAppVersion());
    }

    @Override
    public User registerUser(
            String username,
            String email,
            String displayName) {

        String normalizedUsername =
                requireText(username, "Логин", 3, 30);

        if (!normalizedUsername.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException(
                    "Некорректный формат логина.");
        }

        String normalizedEmail =
                normalizeEmail(email);

        String normalizedDisplayName =
                requireText(displayName, "Имя", 2, 80);

        ensureUsernameAndEmailAreUnique(
                normalizedUsername, normalizedEmail);

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setDisplayName(normalizedDisplayName);
        user.setRegisteredAt(LocalDateTime.now());
        user.setRole(UserRole.MEMBER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    @Override
    public User updateProfile(
            Long id,
            String email,
            String displayName) {

        User user = findRequiredUser(id);

        String normalizedEmail = normalizeEmail(email);
        String normalizedDisplayName =
                requireText(displayName, "Имя", 2, 80);

        if (!user.getEmail().equalsIgnoreCase(normalizedEmail)
                && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Пользователь с таким email уже существует.");
        }

        user.setEmail(normalizedEmail);
        user.setDisplayName(normalizedDisplayName);

        return userRepository.save(user);
    }

    @Override
    public User changeStatus(Long id, UserStatus status) {
        User user = findRequiredUser(id);

        if (status == null) {
            throw new IllegalArgumentException(
                    "Статус не может быть пустым.");
        }

        user.setStatus(status);
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        User user = findRequiredUser(id);
        userRepository.delete(user);
    }

    private User findRequiredUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Id пользователя не может быть пустым.");
        }

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Пользователь с id " + id + " не найден."));
    }

    private void ensureUsernameAndEmailAreUnique(
            String username,
            String email) {

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException(
                    "Пользователь с таким логином уже существует.");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Пользователь с таким email уже существует.");
        }
    }

    private String normalizeEmail(String email) {
        String normalized = requireText(email, "Email", 5, 254)
                .toLowerCase(Locale.ROOT);

        if (!normalized.matches(
                "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")) {
            throw new IllegalArgumentException("Некорректный email.");
        }

        return normalized;
    }

    private String requireText(
            String value,
            String fieldName,
            int minLength,
            int maxLength) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " не может быть пустым.");
        }

        String normalized = value.trim();

        if (normalized.length() < minLength
                || normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " должен содержать от "
                            + minLength + " до " + maxLength
                            + " символов.");
        }

        return normalized;
    }
}