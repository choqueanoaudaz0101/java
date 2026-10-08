package br.com.fiap.gamefinder.controller;

import br.com.fiap.gamefinder.assembler.GameModelAssembler;
import br.com.fiap.gamefinder.dto.GameResponse;
import br.com.fiap.gamefinder.service.GameService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;
    private final GameModelAssembler gameModelAssembler;
    private final PagedResourcesAssembler<GameResponse> pagedResourcesAssembler;

    public GameController(
            GameService gameService,
            GameModelAssembler gameModelAssembler,
            PagedResourcesAssembler<GameResponse> pagedResourcesAssembler) {
        this.gameService = gameService;
        this.gameModelAssembler = gameModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping("/{id}")
    public EntityModel<GameResponse> getGame(@PathVariable @Positive Long id) {
        return gameModelAssembler.toModel(gameService.findById(id));
    }

    @GetMapping
    public PagedModel<?> getGames(
            @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "title,asc")
            @Pattern(regexp = "(id|title|releaseDate|rating),(asc|desc)") String sort) {
        return toModel(gameService.findAll(pageRequest(page, size, sort)));
    }

    @GetMapping("/genres/{genreId}")
    public PagedModel<?> getGamesByGenre(
            @PathVariable @Positive Long genreId,
            @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "title,asc")
            @Pattern(regexp = "(id|title|releaseDate|rating),(asc|desc)") String sort) {
        return toModel(gameService.findByGenre(genreId, pageRequest(page, size, sort)));
    }

    @GetMapping("/platforms/{platformId}")
    public PagedModel<?> getGamesByPlatform(
            @PathVariable @Positive Long platformId,
            @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "title,asc")
            @Pattern(regexp = "(id|title|releaseDate|rating),(asc|desc)") String sort) {
        return toModel(gameService.findByPlatform(platformId, pageRequest(page, size, sort)));
    }

    private PagedModel<?> toModel(Page<GameResponse> games) {
        var ordering = Sort.by(games.getSort().iterator().next());
        var pageable = PageRequest.of(games.getNumber(), games.getSize(), ordering);
        var responsePage = new PageImpl<>(games.getContent(), pageable, games.getTotalElements());
        if (games.isEmpty()) {
            return pagedResourcesAssembler.toEmptyModel(responsePage, GameResponse.class);
        }
        return pagedResourcesAssembler.toModel(responsePage, gameModelAssembler);
    }

    private PageRequest pageRequest(int page, int size, String sort) {
        var parts = sort.split(",");
        var ordering = Sort.by(Sort.Direction.fromString(parts[1]), parts[0]);
        if (!parts[0].equals("id")) {
            ordering = ordering.and(Sort.by("id"));
        }
        return PageRequest.of(page, size, ordering);
    }
}
