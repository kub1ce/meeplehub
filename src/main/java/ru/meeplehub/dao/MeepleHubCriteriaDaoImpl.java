package ru.meeplehub.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.meeplehub.domain.entity.Registration;
import ru.meeplehub.domain.entity.Review;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class MeepleHubCriteriaDaoImpl implements MeepleHubCriteriaDao {

    private final EntityManager entityManager;

    public MeepleHubCriteriaDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Registration> findRegistrationsByStatusAndPeriod(
            RegistrationStatus status,
            LocalDateTime from,
            LocalDateTime to) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Registration> query =
                cb.createQuery(Registration.class);

        Root<Registration> registration =
                query.from(Registration.class);

        Path<LocalDateTime> registeredAt =
                registration.get("registeredAt");

        Predicate statusPredicate =
                cb.equal(registration.get("status"), status);

        Predicate datePredicate =
                cb.between(registeredAt, from, to);

        query.select(registration)
                .where(cb.and(statusPredicate, datePredicate));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Review> findReviewsByAuthorUsername(String username) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> query = cb.createQuery(Review.class);

        Root<Review> review = query.from(Review.class);

        Join<Review, User> user =
                review.join("user", JoinType.INNER);

        query.select(review)
                .where(cb.equal(user.get("username"), username));

        return entityManager.createQuery(query).getResultList();
    }
}