package ru.meeplehub.repository;

import org.springframework.data.repository.CrudRepository;
import ru.meeplehub.domain.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}