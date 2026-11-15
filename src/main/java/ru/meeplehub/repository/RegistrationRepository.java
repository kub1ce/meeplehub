package ru.meeplehub.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;
import ru.meeplehub.domain.entity.Registration;
import ru.meeplehub.domain.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.List;

@RepositoryRestResource(
        collectionResourceRel = "registrations",
        path = "registrations"
)
public interface RegistrationRepository
        extends CrudRepository<Registration, Long> {

    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "event"})
    @RestResource(path = "by-period", rel = "byPeriod")
    List<Registration> findByStatusAndRegisteredAtBetween(
            @Param("status") RegistrationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @RestResource(exported = false)
    boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);
}