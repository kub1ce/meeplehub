package ru.meeplehub.service;

import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.UserStatus;

import java.util.List;
import java.util.Optional;

/** Бизнес-операции над пользователями платформы. */
public interface UserService {

    User registerUser(
            String username,
            String email,
            String displayName,
            String password
    );

    Optional<User> findById(Long id);

    List<User> findAll();

    User updateProfile(Long id, String email, String displayName);

    User changeStatus(Long id, UserStatus status);

    void deleteById(Long id);
}
