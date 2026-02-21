package org.joinmastodon.web.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Security tests for authentication mechanisms.
 * Tests auth bypass attempts and token validation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("Authentication Security Tests")
class AuthenticationSecurityTest {

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
    private User user;
    private Application app;
    private String validToken;

    @BeforeEach
    void setup() {
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            entityManager.createNativeQuery(
                    "TRUNCATE TABLE accounts, users, oauth_access_tokens, applications CASCADE")
                    .executeUpdate();

            account = new Account();
            account.setUsername("alice");
            account.setAcct("alice@local");
            account.setDisplayName("Alice");
            entityManager.persist(account);

            user = new User();
            user.setAccount(account);
            user.setEmail("alice@example.test");
            user.setPasswordHash("hashed_password");
            user.setLocale("en");
            entityManager.persist(user);

            app = new Application();
            app.setName("Test App");
            app.setClientId("test_client_id");
            app.setClientSecret("test_client_secret");
            app.setScopes("read write");
            entityManager.persist(app);

            OAuthAccessToken token = new OAuthAccessToken();
            token.setToken("valid_token_12345");
            token.setUser(user);
            token.setApplication(app);
            token.setScopes("read write");
            token.setCreatedAt(Instant.now());
            entityManager.persist(token);
            validToken = token.getToken();

            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidationTests {

        @Test
        @DisplayName("Rejects invalid token format")
        void rejectsInvalidTokenFormat() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("invalid_token");

            try {
                restTemplate.exchange(
                        baseUrl("/api/v1/accounts/verify_credentials"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Rejects empty token")
        void rejectsEmptyToken() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("");

            try {
                restTemplate.exchange(
                        baseUrl("/api/v1/accounts/verify_credentials"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Rejects malformed Authorization header")
        void rejectsMalformedAuthorizationHeader() {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer invalid format");

            try {
                restTemplate.exchange(
                        baseUrl("/api/v1/accounts/verify_credentials"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Nested
    @DisplayName("Auth Bypass Attempts")
    class AuthBypassTests {

        @Test
        @DisplayName("Rejects SQL injection in token")
        void rejectsSqlInjectionInToken() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("' OR '1'='1");

            try {
                restTemplate.exchange(
                        baseUrl("/api/v1/accounts/verify_credentials"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Rejects null byte injection")
        void rejectsNullByteInjection() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("valid_token\u0000malicious");

            try {
                restTemplate.exchange(
                        baseUrl("/api/v1/accounts/verify_credentials"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
                throw new AssertionError("Expected 400 Bad Request or 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Rejects token with special characters")
        void rejectsTokenWithSpecialCharacters() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("<script>alert('xss')</script>");

            try {
                restTemplate.exchange(
                        baseUrl("/api/v1/accounts/verify_credentials"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Nested
    @DisplayName("Scope Validation")
    class ScopeValidationTests {

        @Test
        @DisplayName("Valid token can access protected endpoint")
        void validTokenCanAccessProtectedEndpoint() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(validToken);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl("/api/v1/accounts/verify_credentials"),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode body = objectMapper.readTree(response.getBody());
            assertThat(body.path("id").asText()).isEqualTo(account.getId().toString());
        }
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
