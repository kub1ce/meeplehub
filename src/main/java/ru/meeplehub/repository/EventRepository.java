package ru.meeplehub.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.meeplehub.domain.entity.Event;

@RepositoryRestResource(
        collectionResourceRel = "events",
        path = "events"
)
public interface EventRepository extends CrudRepository<Event, Long> {
}