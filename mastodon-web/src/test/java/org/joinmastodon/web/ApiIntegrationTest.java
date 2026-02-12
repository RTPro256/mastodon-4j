package org.joinmastodon.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.entity.Mention;
import org.joinmastodon.core.entity.Poll;
import org.joinmastodon.core.entity.PollOption;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.entity.Tag;
import org.joinmastodon.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@Testcontainers
class ApiIntegrationTest {

    static {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("windows")) {
            String dockerHostEnv = System.getenv("DOCKER_HOST");
            if (dockerHostEnv == null || dockerHostEnv.contains("docker_cli")) {
                String dockerHost = "npipe:////./pipe/docker_engine";
                System.setProperty("DOCKER_HOST", dockerHost);
                System.setProperty("docker.host", dockerHost);
                System.setProperty("docker.client.strategy",
                        "org.testcontainers.dockerclient.NpipeSocketClientProviderStrategy");
            }
        }
    }

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("mastodon_test")
            .withUsername("mastodon")
            .withPassword("mastodon");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Value("${local.server.port}")
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private Account account;
    private Status status;
    private MediaAttachment mediaAttachment;
    private Tag tag;
    private Poll poll;

    @BeforeEach
    void setup() {
        account = new Account();
        account.setUsername("alice");
        account.setAcct("alice@local");
        account.setDisplayName("Alice");
        entityManager.persist(account);

        User user = new User();
        user.setAccount(account);
        user.setEmail("alice@example.test");
        user.setPasswordHash("hash");
        entityManager.persist(user);

        status = new Status();
        status.setAccount(account);
        status.setContent("<p>Hello</p>");
        status.setCreatedAt(Instant.parse("2026-02-11T10:00:00Z"));
        entityManager.persist(status);

        mediaAttachment = new MediaAttachment();
        mediaAttachment.setAccountId(account.getId());
        mediaAttachment.setType("image");
        mediaAttachment.setUrl("https://example.test/media/1.png");
        mediaAttachment.setPreviewUrl("https://example.test/media/1-preview.png");
        mediaAttachment.setDescription("test");
        entityManager.persist(mediaAttachment);

        status.getMediaAttachments().add(mediaAttachment);

        tag = new Tag();
        tag.setName("java");
        tag.setUrl("https://example.test/tags/java");
        entityManager.persist(tag);
        status.getTags().add(tag);

        Mention mention = new Mention();
        mention.setStatus(status);
        mention.setAccount(account);
        mention.setUsername("alice");
        mention.setAcct("alice@local");
        mention.setUrl("https://example.test/@alice");
        entityManager.persist(mention);
        status.getMentions().add(mention);

        poll = new Poll();
        poll.setStatus(status);
        poll.setMultiple(false);
        poll.setVotesCount(0);
        poll.setCreatedAt(Instant.parse("2026-02-11T10:00:00Z"));
        entityManager.persist(poll);

        PollOption option = new PollOption();
        option.setPoll(poll);
        option.setTitle("Yes");
        option.setVotesCount(0);
        option.setPosition(0);
        entityManager.persist(option);
        poll.setOptions(List.of(option));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void statusEndpointIncludesExtras() throws Exception {
        JsonNode body = getJson("/api/v1/statuses/" + status.getId());
        assertThat(body.path("media_attachments").path(0).path("id").asText())
                .isEqualTo(mediaAttachment.getId().toString());
        assertThat(body.path("mentions").path(0).path("acct").asText())
                .isEqualTo("alice@local");
        assertThat(body.path("tags").path(0).path("name").asText())
                .isEqualTo("java");
        assertThat(body.path("poll").path("id").asText())
                .isEqualTo(poll.getId().toString());
        assertThat(body.path("poll").path("options").path(0).path("title").asText())
                .isEqualTo("Yes");
    }

    @Test
    void mediaEndpointReturnsAttachment() throws Exception {
        JsonNode body = getJson("/api/v1/media/" + mediaAttachment.getId());
        assertThat(body.path("id").asText()).isEqualTo(mediaAttachment.getId().toString());
        assertThat(body.path("type").asText()).isEqualTo("image");
    }

    @Test
    void pollEndpointReturnsPoll() throws Exception {
        JsonNode body = getJson("/api/v1/polls/" + poll.getId());
        assertThat(body.path("id").asText()).isEqualTo(poll.getId().toString());
        assertThat(body.path("options").path(0).path("title").asText())
                .isEqualTo("Yes");
    }

    private JsonNode getJson(String path) throws IOException {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl(path), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return objectMapper.readTree(response.getBody());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
