package org.joinmastodon.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.OAuthAccessToken;
import org.joinmastodon.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for input validation and sanitization.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Input Validation Tests")
class InputValidationTest {

    static {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("windows")) {
            System.setProperty("ryuk.disabled", "true");
            System.setProperty("testcontainers.ryuk.disabled", "true");
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
    }

    @Value("${local.server.port}")
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String accessToken;

    @BeforeEach
    void setup() {
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            entityManager.createNativeQuery(
                    "TRUNCATE TABLE accounts, users, oauth_access_tokens, applications, statuses CASCADE")
                    .executeUpdate();

            Account account = new Account();
            account.setUsername("alice");
            account.setAcct("alice@local");
            account.setDisplayName("Alice");
            entityManager.persist(account);

            User user = new User();
            user.setAccount(account);
            user.setEmail("alice@example.test");
            user.setPasswordHash("hashed_password");
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

            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("Status Content Validation")
    class StatusContentValidationTests {

        @Test
        @DisplayName("Accepts valid status content")
        void acceptsValidStatusContent() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            String payload = """
                {
                  "status": "Hello world! This is a valid status.",
                  "visibility": "public"
                }
                """;

            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            var response = restTemplate.postForEntity(baseUrl("/api/v1/statuses"), request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Rejects empty status without media")
        void rejectsEmptyStatusWithoutMedia() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            String payload = """
                {
                  "status": "",
                  "visibility": "public"
                }
                """;

            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            try {
                restTemplate.postForEntity(baseUrl("/api/v1/statuses"), request, String.class);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }

        @Test
        @DisplayName("Handles very long status content")
        void handlesVeryLongStatusContent() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            // Create a very long status (over 500 characters)
            StringBuilder longContent = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                longContent.append("This is a very long status message. ");
            }

            String payload = String.format("""
                {
                  "status": "%s",
                  "visibility": "public"
                }
                """, longContent.toString().replace("\"", "\\\""));

            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            // Should either accept or reject gracefully, not crash
            try {
                var response = restTemplate.postForEntity(baseUrl("/api/v1/statuses"), request, String.class);
                // If accepted, verify it's a valid response
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            } catch (HttpClientErrorException ex) {
                // If rejected, should be a proper error
                assertThat(ex.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.PAYLOAD_TOO_LARGE);
            }
        }
    }

    @Nested
    @DisplayName("ID Parameter Validation")
    class IdParameterValidationTests {

        @Test
        @DisplayName("Rejects non-numeric ID")
        void rejectsNonNumericId() {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/statuses/abc"), String.class);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }

        @Test
        @DisplayName("Rejects negative ID")
        void rejectsNegativeId() {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/statuses/-1"), String.class);
                throw new AssertionError("Expected 404 Not Found or 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
        }

        @Test
        @DisplayName("Rejects SQL injection in ID")
        void rejectsSqlInjectionInId() {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/statuses/1; DROP TABLE statuses;--"), String.class);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Nested
    @DisplayName("Query Parameter Validation")
    class QueryParameterValidationTests {

        @Test
        @DisplayName("Handles invalid limit parameter")
        void handlesInvalidLimitParameter() {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/timelines/public?limit=abc"), String.class);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }

        @Test
        @DisplayName("Handles negative limit parameter")
        void handlesNegativeLimitParameter() {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/timelines/public?limit=-10"), String.class);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }

        @Test
        @DisplayName("Handles excessively large limit parameter")
        void handlesExcessivelyLargeLimitParameter() {
            // Should cap the limit, not crash
            var response = restTemplate.getForEntity(baseUrl("/api/v1/timelines/public?limit=999999"), String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("JSON Payload Validation")
    class JsonPayloadValidationTests {

        @Test
        @DisplayName("Rejects malformed JSON")
        void rejectsMalformedJson() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            String malformedJson = "{ invalid json }";

            HttpEntity<String> request = new HttpEntity<>(malformedJson, headers);

            try {
                restTemplate.postForEntity(baseUrl("/api/v1/statuses"), request, String.class);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }

        @Test
        @DisplayName("Rejects empty JSON object")
        void rejectsEmptyJsonObject() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            try {
                restTemplate.postForEntity(baseUrl("/api/v1/statuses"), request, String.class);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
