package ru.meeplehub.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.meeplehub.domain.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepository implements CrudRepository<User, Long> {

    private final List<User> userStorage;

    public UserRepository(@Qualifier("userStorage") List<User> userStorage) {
        this.userStorage = Objects.requireNonNull(userStorage, "userStorage must not be null");
    }

    @Override
    public synchronized User create(User user) {
        Objects.requireNonNull(user, "user must not be null");
        if (user.getId() == null) {
            throw new IllegalArgumentException("У пользователя должен быть задан id.");
        }
        if (userStorage.stream().anyMatch(existing -> existing.getId().equals(user.getId()))) {
            throw new IllegalArgumentException("Пользователь с id " + user.getId() + " уже существует.");
        }
        userStorage.add(user);
        return user;
    }

    @Override
    public synchronized Optional<User> read(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userStorage.stream()
                .filter(user -> id.equals(user.getId()))
                .findFirst();
    }

    @Override
    public synchronized List<User> readAll() {
        return new ArrayList<>(userStorage);
    }

    @Override
    public synchronized User update(User updatedUser) {
        Objects.requireNonNull(updatedUser, "updatedUser must not be null");
        if (updatedUser.getId() == null) {
            throw new IllegalArgumentException("Нельзя обновить пользователя без id.");
        }

        for (int index = 0; index < userStorage.size(); index++) {
            if (updatedUser.getId().equals(userStorage.get(index).getId())) {
                userStorage.set(index, updatedUser);
                return updatedUser;
            }
        }

        throw new IllegalArgumentException("Пользователь с id " + updatedUser.getId() + " не найден.");
    }

    @Override
    public synchronized boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return userStorage.removeIf(user -> id.equals(user.getId()));
    }
}
