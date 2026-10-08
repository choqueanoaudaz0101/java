package br.com.fiap.gamefinder.repository;

import br.com.fiap.gamefinder.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    @Override
    @EntityGraph(attributePaths = {"genre", "platform"})
    Page<Game> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"genre", "platform"})
    Optional<Game> findById(Long id);

    @EntityGraph(attributePaths = {"genre", "platform"})
    Page<Game> findByGenreId(Long genreId, Pageable pageable);

    @EntityGraph(attributePaths = {"genre", "platform"})
    Page<Game> findByPlatformId(Long platformId, Pageable pageable);
}
