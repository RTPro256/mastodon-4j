package org.joinmastodon.web.security;

import jakarta.persistence.EntityManager;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.OAuthAccessToken;
import org.joinmastodon.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.joinmastodon.web.config.TestSecurityConfig;
import org.joinmastodon.web.conformance.SharedPostgresContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for input validation and sanitization.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("Input Validation Tests")
class InputValidationTest {

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
                throw new AssertionError("Expected 400 Bad Request or 422 Unprocessable Content");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNPROCESSABLE_CONTENT);
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
                assertThat(ex.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatusCode.valueOf(413));
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
                throw new AssertionError("Expected 400 Bad Request or 500 Internal Server Error");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (org.springframework.web.client.HttpServerErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        @Test
        @DisplayName("Handles negative limit parameter")
        void handlesNegativeLimitParameter() {
            try {
                var response = restTemplate.getForEntity(baseUrl("/api/v1/timelines/public?limit=-10"), String.class);
                // If accepted, that's fine - the app handles it gracefully
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            } catch (HttpClientErrorException ex) {
                // If rejected, should be a client error
                assertThat(ex.getStatusCode().is4xxClientError()).isTrue();
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
                throw new AssertionError("Expected 400 Bad Request or 422 Unprocessable Content");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNPROCESSABLE_CONTENT);
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
                throw new AssertionError("Expected 400 Bad Request or 422 Unprocessable Content");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNPROCESSABLE_CONTENT);
            }
        }
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
