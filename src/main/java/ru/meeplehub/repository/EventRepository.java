package ru.meeplehub.repository;

import org.springframework.data.repository.CrudRepository;
import ru.meeplehub.domain.entity.Event;

public interface EventRepository extends CrudRepository<Event, Long> {
}