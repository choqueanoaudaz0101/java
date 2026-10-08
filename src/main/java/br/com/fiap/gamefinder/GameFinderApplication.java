package br.com.fiap.gamefinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class GameFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameFinderApplication.class, args);
    }
}
