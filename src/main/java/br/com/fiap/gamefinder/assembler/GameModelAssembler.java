package br.com.fiap.gamefinder.assembler;

import br.com.fiap.gamefinder.controller.GameController;
import br.com.fiap.gamefinder.dto.GameResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GameModelAssembler implements RepresentationModelAssembler<GameResponse, EntityModel<GameResponse>> {

    @Override
    public EntityModel<GameResponse> toModel(GameResponse game) {
        return EntityModel.of(
                game,
                linkTo(methodOn(GameController.class).getGame(game.id()))
                        .withSelfRel().withTitle(game.title()),
                linkTo(GameController.class).slash("genres").slash(game.genre().id())
                        .withRel("same-genre").withTitle("Games in " + game.genre().name() + " genre"),
                linkTo(GameController.class).slash("platforms").slash(game.platform().id())
                        .withRel("same-platform").withTitle("Games on " + game.platform().name()));
    }
}
