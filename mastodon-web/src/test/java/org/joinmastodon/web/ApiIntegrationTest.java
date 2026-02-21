package org.joinmastodon.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.entity.Mention;
import org.joinmastodon.core.entity.OAuthAccessToken;
import org.joinmastodon.core.entity.Poll;
import org.joinmastodon.core.entity.PollOption;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.entity.Tag;
import org.joinmastodon.core.entity.User;
import org.joinmastodon.web.config.TestSecurityConfig;
import org.joinmastodon.web.conformance.SharedPostgresContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ApiIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        // Start and migrate the shared container
        SharedPostgresContainer.startAndMigrate();

        // Register datasource properties
        registry.add("spring.datasource.url", SharedPostgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", SharedPostgresContainer::getUsername);
        registry.add("spring.datasource.password", SharedPostgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", SharedPostgresContainer::getDriverClassName);
    }

    @Value("${local.server.port}")
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Account account;
    private Status status;
    private MediaAttachment mediaAttachment;
    private Tag tag;
    private Poll poll;
    private String accessToken;

    @BeforeEach
    void setup() {
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            entityManager.createNativeQuery(
                            "TRUNCATE TABLE accounts, users, statuses, follows, media_attachments, status_media_attachments, "
                                    + "tags, statuses_tags, mentions, polls, poll_options, poll_votes, notifications, applications, "
                                    + "lists, list_accounts, filters, filter_keywords, reports, report_statuses, oauth_access_tokens CASCADE")
                    .executeUpdate();
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

            Application app = new Application();
            app.setName("Test App");
            app.setClientId("test_client");
            app.setClientSecret("test_secret");
            app.setScopes("read write");
            entityManager.persist(app);

            OAuthAccessToken token = new OAuthAccessToken();
            token.setToken("test_token_" + System.nanoTime());
            token.setUser(user);
            token.setApplication(app);
            token.setScopes("read write");
            token.setCreatedAt(Instant.now());
            entityManager.persist(token);
            accessToken = token.getToken();

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
        });
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
        JsonNode body = getJsonWithAuth("/api/v1/media/" + mediaAttachment.getId(), accessToken);
        assertThat(body.path("id").asText()).isEqualTo(mediaAttachment.getId().toString());
        assertThat(body.path("type").asText()).isEqualTo("image");
    }

    @Test
    void pollEndpointReturnsPoll() throws Exception {
        JsonNode body = getJsonWithAuth("/api/v1/polls/" + poll.getId(), accessToken);
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

    private JsonNode getJsonWithAuth(String path, String token) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl(path), org.springframework.http.HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return objectMapper.readTree(response.getBody());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
