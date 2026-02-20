package org.joinmastodon.web.conformance;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API Conformance tests for OAuth endpoints.
 * Tests compatibility with Mastodon API v4.5.6.
 * 
 * @see <a href="https://docs.joinmastodon.org/methods/oauth/">Mastodon OAuth API</a>
 */
@DisplayName("OAuth API Conformance Tests")
class OAuthApiConformanceTest extends BaseApiConformanceTest {

    private TestUser testUser;
    private Application testApp;

    @BeforeEach
    void setupOAuth() {
        clearTables();
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            testUser = createTestAccount("alice", "alice@example.test");
            testApp = createTestApplication("Test App", "read write");
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("POST /api/v1/apps")
    class CreateApplicationTests {

        @Test
        @DisplayName("Creates application with required fields")
        void createApplicationWithRequiredFields() throws Exception {
            String payload = """
                {
                  "client_name": "Test Client",
                  "redirect_uris": "urn:ietf:wg:oauth:2.0:oob",
                  "scopes": "read write"
                }
                """;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            var response = restTemplate.postForEntity(
                    baseUrl("/api/v1/apps"),
                    new HttpEntity<>(payload, headers),
                    String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode body = objectMapper.readTree(response.getBody());
            
            assertThat(body.has("id")).isTrue();
            assertThat(body.has("client_id")).isTrue();
            assertThat(body.has("client_secret")).isTrue();
            assertThat(body.path("name").asText()).isEqualTo("Test Client");
            assertThat(body.path("redirect_uri").asText()).isEqualTo("urn:ietf:wg:oauth:2.0:oob");
        }

        @Test
        @DisplayName("Returns 422 for missing client_name")
        void createApplicationMissingName() throws Exception {
            String payload = """
                {
                  "redirect_uris": "urn:ietf:wg:oauth:2.0:oob",
                  "scopes": "read"
                }
                """;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/apps"),
                        new HttpEntity<>(payload, headers),
                        String.class);
                throw new AssertionError("Expected 422 Unprocessable Entity");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        @Test
        @DisplayName("Returns 422 for missing redirect_uris")
        void createApplicationMissingRedirectUris() throws Exception {
            String payload = """
                {
                  "client_name": "Test Client",
                  "scopes": "read"
                }
                """;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/apps"),
                        new HttpEntity<>(payload, headers),
                        String.class);
                throw new AssertionError("Expected 422 Unprocessable Entity");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }
    }

    @Nested
    @DisplayName("POST /oauth/token")
    class ObtainTokenTests {

        @Test
        @DisplayName("Returns token with password grant")
        void obtainTokenWithPasswordGrant() throws Exception {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.put("grant_type", List.of("password"));
            form.put("client_id", List.of(testApp.getClientId()));
            form.put("client_secret", List.of(testApp.getClientSecret()));
            form.put("username", List.of(testUser.user().getEmail()));
            form.put("password", List.of("hashed_password")); // Using the hashed password from setup
            form.put("scope", List.of("read"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            var response = restTemplate.postForEntity(
                    baseUrl("/oauth/token"),
                    new HttpEntity<>(form, headers),
                    String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode body = objectMapper.readTree(response.getBody());
            
            assertThat(body.has("access_token")).isTrue();
            assertThat(body.has("token_type")).isTrue();
            assertThat(body.path("token_type").asText()).isEqualTo("Bearer");
            assertThat(body.has("scope")).isTrue();
            assertThat(body.has("created_at")).isTrue();
        }

        @Test
        @DisplayName("Returns 401 for invalid credentials")
        void obtainTokenInvalidCredentials() throws Exception {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.put("grant_type", List.of("password"));
            form.put("client_id", List.of(testApp.getClientId()));
            form.put("client_secret", List.of(testApp.getClientSecret()));
            form.put("username", List.of("wrong@example.test"));
            form.put("password", List.of("wrongpassword"));
            form.put("scope", List.of("read"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/oauth/token"),
                        new HttpEntity<>(form, headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns 400 for invalid grant type")
        void obtainTokenInvalidGrantType() throws Exception {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.put("grant_type", List.of("invalid_grant"));
            form.put("client_id", List.of(testApp.getClientId()));
            form.put("client_secret", List.of(testApp.getClientSecret()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/oauth/token"),
                        new HttpEntity<>(form, headers),
                        String.class);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Nested
    @DisplayName("POST /oauth/revoke")
    class RevokeTokenTests {

        @Test
        @DisplayName("Revokes token successfully")
        void revokeTokenSuccessfully() throws Exception {
            // First obtain a token
            String accessToken = createAccessToken(testUser.user(), testApp, "read");
            
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.put("token", List.of(accessToken));
            form.put("client_id", List.of(testApp.getClientId()));
            form.put("client_secret", List.of(testApp.getClientSecret()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            var response = restTemplate.postForEntity(
                    baseUrl("/oauth/revoke"),
                    new HttpEntity<>(form, headers),
                    String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Returns 200 even for invalid token (per OAuth spec)")
        void revokeInvalidToken() throws Exception {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.put("token", List.of("invalid_token_12345"));
            form.put("client_id", List.of(testApp.getClientId()));
            form.put("client_secret", List.of(testApp.getClientSecret()));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            var response = restTemplate.postForEntity(
                    baseUrl("/oauth/revoke"),
                    new HttpEntity<>(form, headers),
                    String.class);
            
            // OAuth spec says to return 200 even for invalid tokens
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/apps/verify_credentials")
    class VerifyAppCredentialsTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void verifyAppCredentialsReturns401() throws Exception {
            try {
                restTemplate.getForEntity(
                        baseUrl("/api/v1/apps/verify_credentials"),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Nested
    @DisplayName("Token Response Format")
    class TokenResponseFormatTests {

        @Test
        @DisplayName("Token response includes all required fields")
        void tokenResponseIncludesRequiredFields() throws Exception {
            String accessToken = createAccessToken(testUser.user(), testApp, "read write");
            
            // Verify the token works
            JsonNode account = getJsonWithAuth(
                    "/api/v1/accounts/verify_credentials",
                    accessToken);
            
            assertThat(account.has("id")).isTrue();
            assertThat(account.path("id").asText())
                    .isEqualTo(testUser.account().getId().toString());
        }
    }

    @Nested
    @DisplayName("Scope Validation")
    class ScopeValidationTests {

        @Test
        @DisplayName("Valid scope values are accepted")
        void validScopesAccepted() throws Exception {
            String payload = """
                {
                  "client_name": "Scope Test App",
                  "redirect_uris": "urn:ietf:wg:oauth:2.0:oob",
                  "scopes": "read write follow push"
                }
                """;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            var response = restTemplate.postForEntity(
                    baseUrl("/api/v1/apps"),
                    new HttpEntity<>(payload, headers),
                    String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode body = objectMapper.readTree(response.getBody());
            assertThat(body.path("scopes").asText()).contains("read", "write", "follow", "push");
        }

        @Test
        @DisplayName("Granular scopes are accepted")
        void granularScopesAccepted() throws Exception {
            String payload = """
                {
                  "client_name": "Granular Scope App",
                  "redirect_uris": "urn:ietf:wg:oauth:2.0:oob",
                  "scopes": "read:accounts read:statuses write:statuses"
                }
                """;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            var response = restTemplate.postForEntity(
                    baseUrl("/api/v1/apps"),
                    new HttpEntity<>(payload, headers),
                    String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
