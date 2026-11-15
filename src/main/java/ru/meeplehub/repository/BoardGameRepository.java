package ru.meeplehub.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.meeplehub.domain.entity.BoardGame;

@RepositoryRestResource(
        collectionResourceRel = "boardGames",
        path = "board-games"
)
public interface BoardGameRepository extends CrudRepository<BoardGame, Long> {
}