package br.com.fiap.gamefinder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(indexes = {
        @Index(name = "idx_game_genre", columnList = "genre_id"),
        @Index(name = "idx_game_platform", columnList = "platform_id"),
        @Index(name = "idx_game_title", columnList = "title")
})
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Column(nullable = false)
    private Double rating;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    @Column(nullable = false, length = 2048)
    private String coverUrl;

    @Column(nullable = false, length = 2048)
    private String backdropUrl;

    @Column(nullable = false)
    private boolean inWishlist;

    protected Game() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public Double getRating() {
        return rating;
    }

    public Genre getGenre() {
        return genre;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getBackdropUrl() {
        return backdropUrl;
    }

    public boolean isInWishlist() {
        return inWishlist;
    }
}
