package ru.meeplehub.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import ru.meeplehub.domain.entity.User;

@RepositoryRestResource(
        collectionResourceRel = "users",
        path = "users"
)
public interface UserRepository extends CrudRepository<User, Long> {

    @RestResource(exported = false)
    boolean existsByUsernameIgnoreCase(String username);

    @RestResource(exported = false)
    boolean existsByEmailIgnoreCase(String email);
}