package br.com.fiap.gamefinder.exception;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException() {
        super("Jogo não encontrado.");
    }
}
