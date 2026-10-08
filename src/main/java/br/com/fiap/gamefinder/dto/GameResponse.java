package br.com.fiap.gamefinder.dto;

import br.com.fiap.gamefinder.model.Game;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;

@Relation(itemRelation = "game", collectionRelation = "gameList")
public record GameResponse(
        Long id,
        String title,
        String description,
        LocalDate releaseDate,
        Double rating,
        GenreResponse genre,
        PlatformResponse platform,
        String coverUrl,
        String backdropUrl,
        boolean inWishlist) {

    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getReleaseDate(),
                game.getRating(),
                new GenreResponse(game.getGenre().getId(), game.getGenre().getName()),
                new PlatformResponse(game.getPlatform().getId(), game.getPlatform().getName()),
                game.getCoverUrl(),
                game.getBackdropUrl(),
                game.isInWishlist());
    }

    public record GenreResponse(Long id, String name) {
    }

    public record PlatformResponse(Long id, String name) {
    }
}
