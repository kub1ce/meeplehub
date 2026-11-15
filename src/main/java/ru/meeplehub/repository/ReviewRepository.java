package ru.meeplehub.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;
import ru.meeplehub.domain.entity.Review;

import java.util.List;

@RepositoryRestResource(
        collectionResourceRel = "reviews",
        path = "reviews"
)
public interface ReviewRepository extends CrudRepository<Review, Long> {

    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"user", "boardGame"})
    @RestResource(path = "by-author", rel = "byAuthor")
    @Query("""
            SELECT r
            FROM Review r
            JOIN r.user u
            WHERE u.username = :username
            """)
    List<Review> findReviewsByAuthorUsername(
            @Param("username") String username
    );
}