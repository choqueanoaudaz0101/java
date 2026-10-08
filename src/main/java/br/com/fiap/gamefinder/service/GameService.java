package br.com.fiap.gamefinder.service;

import br.com.fiap.gamefinder.dto.GameResponse;
import br.com.fiap.gamefinder.exception.GameNotFoundException;
import br.com.fiap.gamefinder.repository.GameRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public GameResponse findById(Long id) {
        return gameRepository.findById(id)
                .map(GameResponse::from)
                .orElseThrow(GameNotFoundException::new);
    }

    public Page<GameResponse> findAll(Pageable pageable) {
        return gameRepository.findAll(pageable).map(GameResponse::from);
    }

    public Page<GameResponse> findByGenre(Long genreId, Pageable pageable) {
        return gameRepository.findByGenreId(genreId, pageable).map(GameResponse::from);
    }

    public Page<GameResponse> findByPlatform(Long platformId, Pageable pageable) {
        return gameRepository.findByPlatformId(platformId, pageable).map(GameResponse::from);
    }
}
