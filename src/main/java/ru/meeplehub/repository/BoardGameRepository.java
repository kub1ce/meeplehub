package ru.meeplehub.repository;

import org.springframework.data.repository.CrudRepository;
import ru.meeplehub.domain.entity.BoardGame;

public interface BoardGameRepository extends CrudRepository<BoardGame, Long> {
}