package ru.meeplehub.dao;

import ru.meeplehub.domain.entity.Registration;
import ru.meeplehub.domain.entity.Review;
import ru.meeplehub.domain.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface MeepleHubCriteriaDao {

    List<Registration> findRegistrationsByStatusAndPeriod(
            RegistrationStatus status,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Review> findReviewsByAuthorUsername(String username);
}