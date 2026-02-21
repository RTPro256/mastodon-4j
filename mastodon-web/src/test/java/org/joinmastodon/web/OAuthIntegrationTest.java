package org.joinmastodon.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class OAuthIntegrationTest {

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

    @BeforeEach
    void setup() {
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            entityManager.createNativeQuery(
                            "TRUNCATE TABLE accounts, users, statuses, follows, media_attachments, status_media_attachments, "
                                    + "tags, statuses_tags, mentions, polls, poll_options, poll_votes, notifications, applications, "
                                    + "lists, list_accounts, filters, filter_keywords, reports, report_statuses, oauth_access_tokens, "
                                    + "oauth_refresh_tokens, oauth_authorization_codes, sessions, rate_limits CASCADE")
                    .executeUpdate();

            account = new Account();
            account.setUsername("oauth");
            account.setAcct("oauth@local");
            account.setDisplayName("OAuth User");
            entityManager.persist(account);

            user = new User();
            user.setAccount(account);
            user.setEmail("oauth@example.test");
            user.setPasswordHash("secret");
            user.setLocale("en");
            user.setLastSignInAt(Instant.parse("2026-02-12T00:00:00Z"));
            entityManager.persist(user);

            entityManager.flush();
            entityManager.clear();
        });
    }

    @Test
    void passwordGrantAllowsVerifyCredentials() throws Exception {
        OAuthClient client = createApplication("read write");
        Token token = issuePasswordToken(client);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.accessToken());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/v1/accounts/verify_credentials"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("id").asText()).isEqualTo(account.getId().toString());
        assertThat(body.path("acct").asText()).isEqualTo("oauth@local");
    }

    @Test
    void revokedTokenIsRejected() throws Exception {
        OAuthClient client = createApplication("read");
        Token token = issuePasswordToken(client);

        revokeToken(token.accessToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.accessToken());
        try {
            restTemplate.exchange(
                    baseUrl("/api/v1/accounts/verify_credentials"),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            throw new AssertionError("Expected 401 Unauthorized");
        } catch (HttpClientErrorException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            JsonNode body = objectMapper.readTree(ex.getResponseBodyAsString());
            assertThat(body.path("error").asText()).isEqualTo("invalid_token");
        }
    }

    private OAuthClient createApplication(String scopes) throws IOException {
        String payload = """
                {
                  "client_name": "Test App",
                  "redirect_uris": "urn:ietf:wg:oauth:2.0:oob",
                  "scopes": "%s",
                  "website": "https://example.test"
                }
                """.formatted(scopes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl("/api/v1/apps"),
                new HttpEntity<>(payload, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        return new OAuthClient(
                body.path("client_id").asText(),
                body.path("client_secret").asText(),
                body.path("scopes").asText());
    }

    private Token issuePasswordToken(OAuthClient client) throws IOException {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.put("grant_type", List.of("password"));
        form.put("client_id", List.of(client.clientId()));
        form.put("client_secret", List.of(client.clientSecret()));
        form.put("username", List.of(user.getEmail()));
        form.put("password", List.of("secret"));
        form.put("scope", List.of("read"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl("/oauth/token"),
                new HttpEntity<>(form, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        return new Token(
                body.path("access_token").asText(),
                body.path("refresh_token").asText());
    }

    private void revokeToken(String token) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.put("token", List.of(token));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl("/oauth/revoke"),
                new HttpEntity<>(form, headers),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private record OAuthClient(String clientId, String clientSecret, String scopes) {
    }

    private record Token(String accessToken, String refreshToken) {
    }
}
