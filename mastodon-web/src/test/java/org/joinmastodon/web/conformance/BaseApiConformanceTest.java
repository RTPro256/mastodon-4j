package org.joinmastodon.web.conformance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.flywaydb.core.Flyway;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.User;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.entity.MediaAttachment;
import org.joinmastodon.core.entity.Notification;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.OAuthAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for API conformance tests.
 * Provides common test infrastructure and helper methods for testing
 * Mastodon API v4.5.6 compatibility.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseApiConformanceTest {

    static {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("windows")) {
            System.setProperty("ryuk.disabled", "true");
            System.setProperty("testcontainers.ryuk.disabled", "true");
            System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
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

    private static final AtomicBoolean MIGRATED = new AtomicBoolean(false);

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        if (MIGRATED.compareAndSet(false, true)) {
            Flyway.configure()
                    .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();
        }
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Value("${local.server.port}")
    protected int port;

    protected final RestTemplate restTemplate = new RestTemplate();
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    // Helper method to get JSON response
    protected JsonNode getJson(String path) throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl(path), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return objectMapper.readTree(response.getBody());
    }

    // Helper method to get JSON with auth header
    protected JsonNode getJsonWithAuth(String path, String token) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl(path),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return objectMapper.readTree(response.getBody());
    }

    // Helper method to POST JSON with auth
    protected JsonNode postJsonWithAuth(String path, Object body, String token) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl(path),
                new HttpEntity<>(objectMapper.writeValueAsString(body), headers),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return objectMapper.readTree(response.getBody());
    }

    // Helper method for expecting error responses
    protected void expectError(String path, HttpMethod method, String token, HttpStatus expectedStatus) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        try {
            restTemplate.exchange(
                    baseUrl(path),
                    method,
                    new HttpEntity<>(headers),
                    String.class);
            throw new AssertionError("Expected " + expectedStatus + " but request succeeded");
        } catch (HttpClientErrorException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(expectedStatus);
        }
    }

    // Helper to verify error response format
    protected void verifyErrorResponse(HttpClientErrorException ex, String expectedError) throws Exception {
        JsonNode body = objectMapper.readTree(ex.getResponseBodyAsString());
        assertThat(body.has("error")).isTrue();
        if (expectedError != null) {
            assertThat(body.path("error").asText()).containsIgnoringCase(expectedError);
        }
    }

    protected String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    // Test user record holding both account and user entities
    protected record TestUser(Account account, User user) {}
    
    // Create test account with user
    protected TestUser createTestAccount(String username, String email) {
        Account account = new Account();
        account.setUsername(username);
        account.setAcct(username + "@local");
        account.setDisplayName(username);
        account.setNote("Test account for " + username);
        account.setUrl("https://local/@" + username);
        entityManager.persist(account);

        User user = new User();
        user.setAccount(account);
        user.setEmail(email);
        user.setPasswordHash("hashed_password");
        user.setLocale("en");
        user.setLastSignInAt(Instant.now());
        entityManager.persist(user);

        return new TestUser(account, user);
    }

    // Create OAuth application
    protected Application createTestApplication(String name, String scopes) {
        Application app = new Application();
        app.setName(name);
        app.setRedirectUri("urn:ietf:wg:oauth:2.0:oob");
        app.setScopes(scopes);
        app.setClientId("test_client_" + System.nanoTime());
        app.setClientSecret("test_secret_" + System.nanoTime());
        entityManager.persist(app);
        return app;
    }

    // Create access token for account
    protected String createAccessToken(User user, Application app, String scopes) {
        OAuthAccessToken token = new OAuthAccessToken();
        token.setToken("test_token_" + System.nanoTime());
        token.setUser(user);
        token.setApplication(app);
        token.setScopes(scopes);
        token.setCreatedAt(Instant.now());
        entityManager.persist(token);
        return token.getToken();
    }

    // Clear all tables
    protected void clearTables() {
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            entityManager.createNativeQuery(
                    "TRUNCATE TABLE accounts, users, statuses, follows, media_attachments, status_media_attachments, "
                            + "tags, statuses_tags, mentions, polls, poll_options, poll_votes, notifications, applications, "
                            + "lists, list_accounts, filters, filter_keywords, reports, report_statuses, oauth_access_tokens, "
                            + "oauth_refresh_tokens, oauth_authorization_codes, sessions, rate_limits, blocks, mutes, "
                            + "favourites, bookmarks, domain_blocks, federation_audit_logs CASCADE")
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();
        });
    }

    // Verify pagination link header format
    protected void verifyPaginationHeader(String linkHeader, String expectedRel) {
        assertThat(linkHeader).isNotNull();
        assertThat(linkHeader).contains("rel=\"" + expectedRel + "\"");
        assertThat(linkHeader).contains("<");
        assertThat(linkHeader).contains(">");
    }

    // Verify account response format
    protected void verifyAccountFormat(JsonNode account) {
        assertThat(account.has("id")).isTrue();
        assertThat(account.has("username")).isTrue();
        assertThat(account.has("acct")).isTrue();
        assertThat(account.has("display_name")).isTrue();
        assertThat(account.has("locked")).isTrue();
        assertThat(account.has("bot")).isTrue();
        assertThat(account.has("created_at")).isTrue();
        assertThat(account.has("note")).isTrue();
        assertThat(account.has("url")).isTrue();
        assertThat(account.has("avatar")).isTrue();
        assertThat(account.has("header")).isTrue();
        assertThat(account.has("followers_count")).isTrue();
        assertThat(account.has("following_count")).isTrue();
        assertThat(account.has("statuses_count")).isTrue();
    }

    // Verify status response format
    protected void verifyStatusFormat(JsonNode status) {
        assertThat(status.has("id")).isTrue();
        assertThat(status.has("created_at")).isTrue();
        assertThat(status.has("content")).isTrue();
        assertThat(status.has("account")).isTrue();
        assertThat(status.has("visibility")).isTrue();
        assertThat(status.has("sensitive")).isTrue();
        assertThat(status.has("spoiler_text")).isTrue();
        assertThat(status.has("media_attachments")).isTrue();
        assertThat(status.has("mentions")).isTrue();
        assertThat(status.has("tags")).isTrue();
        assertThat(status.has("uri")).isTrue();
        assertThat(status.has("url")).isTrue();
        assertThat(status.has("favourites_count")).isTrue();
        assertThat(status.has("reblogs_count")).isTrue();
        assertThat(status.has("replies_count")).isTrue();
    }

    // Verify notification response format
    protected void verifyNotificationFormat(JsonNode notification) {
        assertThat(notification.has("id")).isTrue();
        assertThat(notification.has("type")).isTrue();
        assertThat(notification.has("created_at")).isTrue();
        assertThat(notification.has("account")).isTrue();
    }

    // Verify media attachment format
    protected void verifyMediaFormat(JsonNode media) {
        assertThat(media.has("id")).isTrue();
        assertThat(media.has("type")).isTrue();
        assertThat(media.has("url")).isTrue();
        assertThat(media.has("preview_url")).isTrue();
    }
}
