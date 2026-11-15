package ru.meeplehub.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.meeplehub.domain.entity.Registration;
import ru.meeplehub.domain.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistrationRepository
        extends CrudRepository<Registration, Long> {

    @Transactional(readOnly = true)
    List<Registration> findByStatusAndRegisteredAtBetween(
            RegistrationStatus status,
            LocalDateTime from,
            LocalDateTime to
    );

    boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);
}