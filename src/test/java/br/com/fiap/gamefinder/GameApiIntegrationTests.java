package br.com.fiap.gamefinder;

import br.com.fiap.gamefinder.service.GameService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
class GameApiIntegrationTests {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameService gameService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {"/games", "/games/platforms/4", "/games/genres/3"})
    void followsTheGeneratedPaginationLinks(String path) throws Exception {
        var result = mockMvc.perform(get(path).param("size", "2").param("sort", "rating,desc"))
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readTree(result.getResponse().getContentAsString());
        for (String relation : new String[]{"self", "first", "next", "last"}) {
            var link = response.path("_links").path(relation).path("href").asString();
            assertThat(link).isNotBlank();
            mockMvc.perform(get(URI.create(link)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.gameList").isArray());
        }
    }

    @Test
    void listsSeededGamesUsingTheFrontendContract() throws Exception {
        mockMvc.perform(get("/games").param("size", "6").param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.gameList", hasSize(6)))
                .andExpect(jsonPath("$._embedded.gameList[0].id").value(20))
                .andExpect(jsonPath("$._embedded.gameList[0].title").value("Age of Empires IV"))
                .andExpect(jsonPath("$.page.size").value(6))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(20))
                .andExpect(jsonPath("$.page.totalPages").value(4))
                .andExpect(jsonPath("$._links.self.href", containsString("/games")))
                .andExpect(jsonPath("$._links.first.href", containsString("page=0")))
                .andExpect(jsonPath("$._links.next.href", containsString("page=1")))
                .andExpect(jsonPath("$._links.last.href", containsString("page=3")))
                .andExpect(jsonPath("$._links.prev").doesNotExist());
    }

    @Test
    void returnsDetailsAndTheThreeRequiredLinks() throws Exception {
        mockMvc.perform(get("/games/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Legend of Zelda: Tears of the Kingdom"))
                .andExpect(jsonPath("$.releaseDate").value("2023-05-12"))
                .andExpect(jsonPath("$.rating").value(9.5))
                .andExpect(jsonPath("$.genre.id").value(2))
                .andExpect(jsonPath("$.genre.name").value("Adventure"))
                .andExpect(jsonPath("$.platform.id").value(3))
                .andExpect(jsonPath("$.platform.name").value("Nintendo Switch"))
                .andExpect(jsonPath("$.inWishlist").value(true))
                .andExpect(jsonPath("$.coverUrl", containsString("https://")))
                .andExpect(jsonPath("$.backdropUrl", containsString("https://")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/games/1")))
                .andExpect(jsonPath("$._links.self.title").value("The Legend of Zelda: Tears of the Kingdom"))
                .andExpect(jsonPath("$._links['same-genre'].href", endsWith("/games/genres/2")))
                .andExpect(jsonPath("$._links['same-genre'].title").value("Games in Adventure genre"))
                .andExpect(jsonPath("$._links['same-platform'].href", endsWith("/games/platforms/3")))
                .andExpect(jsonPath("$._links['same-platform'].title").value("Games on Nintendo Switch"))
                .andExpect(jsonPath("$._links['remove-from-wishlist']").doesNotExist());
    }

    @Test
    void preservesFalseWishlistValues() throws Exception {
        mockMvc.perform(get("/games/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inWishlist").value(false));
    }

    @Test
    void filtersByGenre() throws Exception {
        mockMvc.perform(get("/games/genres/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.gameList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.gameList[*].genre.id", everyItem(is(4))))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$._links.self.href", containsString("/games/genres/4")));
    }

    @Test
    void filtersByPlatform() throws Exception {
        mockMvc.perform(get("/games/platforms/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.gameList", hasSize(6)))
                .andExpect(jsonPath("$._embedded.gameList[*].platform.id", everyItem(is(4))))
                .andExpect(jsonPath("$.page.totalElements").value(6));
    }

    @Test
    void paginatesFilteredResults() throws Exception {
        mockMvc.perform(get("/games/platforms/4").param("size", "2").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.gameList", hasSize(2)))
                .andExpect(jsonPath("$.page.totalElements").value(6))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$._links.prev.href", containsString("page=0")))
                .andExpect(jsonPath("$._links.next.href", containsString("page=2")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/games/genres/999", "/games/platforms/999", "/games?page=100"})
    void preservesEmptyCollections(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.gameList", hasSize(0)))
                .andExpect(jsonPath("$.page").exists());
    }

    @Test
    void returns404WithoutInternalDetails() throws Exception {
        mockMvc.perform(get("/games/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Jogo não encontrado."))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/games?page=-1",
            "/games?page=10001",
            "/games?page=2147483648",
            "/games?size=0",
            "/games?size=101",
            "/games?size=abc",
            "/games?sort=description,asc",
            "/games?sort=title,invalid",
            "/games?sort=genre.name,asc",
            "/games/0",
            "/games/-1",
            "/games/abc",
            "/games/genres/0",
            "/games/platforms/-1"
    })
    void rejectsInvalidParameters(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @Test
    void ordersByRatingDescending() throws Exception {
        mockMvc.perform(get("/games").param("sort", "rating,desc").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.gameList[0].title").value("Portal 2"));
    }

    @Test
    void ordersByReleaseDate() throws Exception {
        mockMvc.perform(get("/games").param("sort", "releaseDate,asc").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.gameList[0].title").value("Portal 2"));
    }

    @Test
    void returnsOnlyTheRemainingItemsOnTheLastPage() throws Exception {
        mockMvc.perform(get("/games").param("page", "3").param("size", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.gameList", hasSize(2)))
                .andExpect(jsonPath("$._links.prev").exists())
                .andExpect(jsonPath("$._links.next").doesNotExist());
    }

    @Test
    void supportsHeadWithoutResponseBody() throws Exception {
        try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/games/1"))
                    .timeout(Duration.ofSeconds(5))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEmpty();
        }
    }

    @Test
    void setsSecurityHeadersAndDoesNotCreateASession() throws Exception {
        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void rejectsWritesEvenWithAValidCsrfToken() throws Exception {
        mockMvc.perform(post("/games").with(csrf())).andExpect(status().isForbidden());
        mockMvc.perform(put("/games/1").with(csrf())).andExpect(status().isForbidden());
        mockMvc.perform(patch("/games/1").with(csrf())).andExpect(status().isForbidden());
        mockMvc.perform(delete("/games/1").with(csrf())).andExpect(status().isForbidden());
        mockMvc.perform(get("/games/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Legend of Zelda: Tears of the Kingdom"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/h2-console", "/actuator/env", "/admin", "/games/wishlist/1"})
    void rejectsUnexposedRoutes(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isForbidden());
    }

    @Test
    void doesNotGrantCrossOriginAccess() throws Exception {
        mockMvc.perform(get("/games").header("Origin", "https://untrusted.example"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void rejectsSqlInjectionAsASortField() throws Exception {
        mockMvc.perform(get("/games").param("sort", "title;DROP TABLE game,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("DROP TABLE"))));
        mockMvc.perform(get("/games")).andExpect(jsonPath("$.page.totalElements").value(20));
    }

    @Test
    void loadsThePageWithTwoDatabaseQueries() {
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        var games = gameService.findAll(PageRequest.of(0, 6, Sort.by("title")));

        assertThat(games.getContent()).hasSize(6);
        assertThat(games.getTotalElements()).isEqualTo(20);
        assertThat(games.getContent()).allSatisfy(game -> {
            assertThat(game.genre().name()).isNotBlank();
            assertThat(game.platform().name()).isNotBlank();
        });
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }
}
