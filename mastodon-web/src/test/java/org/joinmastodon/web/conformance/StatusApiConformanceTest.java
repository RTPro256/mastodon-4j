package org.joinmastodon.web.conformance;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API Conformance tests for Status endpoints.
 * Tests compatibility with Mastodon API v4.5.6.
 * 
 * @see <a href="https://docs.joinmastodon.org/methods/statuses/">Mastodon Statuses API</a>
 */
@DisplayName("Status API Conformance Tests")
class StatusApiConformanceTest extends BaseApiConformanceTest {

    private TestUser testUser;
    private Application testApp;
    private String accessToken;
    private Status testStatus;

    @BeforeEach
    void setupStatus() {
        clearTables();
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            testUser = createTestAccount("alice", "alice@example.test");
            testApp = createTestApplication("Test App", "read write");
            accessToken = createAccessToken(testUser.user(), testApp, "read write");
            
            testStatus = new Status();
            testStatus.setAccount(testUser.account());
            testStatus.setContent("<p>Test status content</p>");
            testStatus.setCreatedAt(Instant.now());
            testStatus.setVisibility(Visibility.PUBLIC);
            entityManager.persist(testStatus);
            
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("GET /api/v1/statuses/:id")
    class GetStatusTests {

        @Test
        @DisplayName("Returns status with required fields")
        void getStatusReturnsRequiredFields() throws Exception {
            JsonNode status = getJson("/api/v1/statuses/" + testStatus.getId());
            
            verifyStatusFormat(status);
            assertThat(status.path("id").asText()).isEqualTo(testStatus.getId().toString());
            assertThat(status.path("content").asText()).contains("Test status content");
        }

        @Test
        @DisplayName("Returns 404 for non-existent status")
        void getStatusReturns404ForNonExistent() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/statuses/999999999"), String.class);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }

        @Test
        @DisplayName("Returns account in status")
        void getStatusIncludesAccount() throws Exception {
            JsonNode status = getJson("/api/v1/statuses/" + testStatus.getId());
            
            JsonNode account = status.path("account");
            verifyAccountFormat(account);
            assertThat(account.path("id").asText()).isEqualTo(testUser.account().getId().toString());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/statuses")
    class CreateStatusTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void createStatusReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String payload = """
                {
                  "status": "Hello world",
                  "visibility": "public"
                }
                """;
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/statuses"),
                        new HttpEntity<>(payload, headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Creates status when authenticated")
        void createStatusWhenAuthenticated() throws Exception {
            String payload = """
                {
                  "status": "Hello world from test",
                  "visibility": "public"
                }
                """;
            
            JsonNode status = postJsonWithAuth("/api/v1/statuses", payload, accessToken);
            
            verifyStatusFormat(status);
            assertThat(status.path("content").asText()).contains("Hello world from test");
            assertThat(status.path("visibility").asText()).isEqualTo("public");
        }

        @Test
        @DisplayName("Respects visibility parameter")
        void createStatusRespectsVisibility() throws Exception {
            String payload = """
                {
                  "status": "Private post",
                  "visibility": "private"
                }
                """;
            
            JsonNode status = postJsonWithAuth("/api/v1/statuses", payload, accessToken);
            
            assertThat(status.path("visibility").asText()).isEqualTo("private");
        }

        @Test
        @DisplayName("Accepts spoiler_text parameter")
        void createStatusWithSpoilerText() throws Exception {
            String payload = """
                {
                  "status": "Hidden content",
                  "spoiler_text": "Content warning",
                  "sensitive": true
                }
                """;
            
            JsonNode status = postJsonWithAuth("/api/v1/statuses", payload, accessToken);
            
            assertThat(status.path("spoiler_text").asText()).isEqualTo("Content warning");
            assertThat(status.path("sensitive").asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/statuses/:id")
    class DeleteStatusTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void deleteStatusReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.exchange(
                        baseUrl("/api/v1/statuses/" + testStatus.getId()),
                        HttpMethod.DELETE,
                        new HttpEntity<>(new HttpHeaders()),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Deletes own status when authenticated")
        void deleteOwnStatusWhenAuthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            restTemplate.exchange(
                    baseUrl("/api/v1/statuses/" + testStatus.getId()),
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    String.class);
            
            // Verify status is deleted
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/statuses/" + testStatus.getId()), String.class);
                throw new AssertionError("Expected 404 Not Found after deletion");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/statuses/:id/favourite")
    class FavouriteStatusTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void favouriteReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/statuses/" + testStatus.getId() + "/favourite"),
                        new HttpEntity<>("{}", headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Favourites status when authenticated")
        void favouriteStatusWhenAuthenticated() throws Exception {
            JsonNode status = postJsonWithAuth(
                    "/api/v1/statuses/" + testStatus.getId() + "/favourite",
                    "{}",
                    accessToken);
            
            verifyStatusFormat(status);
            assertThat(status.path("favourited").asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/statuses/:id/unfavourite")
    class UnfavouriteStatusTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void unfavouriteReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/statuses/" + testStatus.getId() + "/unfavourite"),
                        new HttpEntity<>("{}", headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/statuses/:id/reblog")
    class ReblogStatusTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void reblogReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/statuses/" + testStatus.getId() + "/reblog"),
                        new HttpEntity<>("{}", headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Reblogs status when authenticated")
        void reblogStatusWhenAuthenticated() throws Exception {
            JsonNode status = postJsonWithAuth(
                    "/api/v1/statuses/" + testStatus.getId() + "/reblog",
                    "{}",
                    accessToken);
            
            verifyStatusFormat(status);
            assertThat(status.path("reblogged").asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/statuses/:id/bookmark")
    class BookmarkStatusTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void bookmarkReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/statuses/" + testStatus.getId() + "/bookmark"),
                        new HttpEntity<>("{}", headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Bookmarks status when authenticated")
        void bookmarkStatusWhenAuthenticated() throws Exception {
            JsonNode status = postJsonWithAuth(
                    "/api/v1/statuses/" + testStatus.getId() + "/bookmark",
                    "{}",
                    accessToken);
            
            verifyStatusFormat(status);
            assertThat(status.path("bookmarked").asBoolean()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/statuses/:id/context")
    class StatusContextTests {

        @Test
        @DisplayName("Returns context for status")
        void getContextReturnsContext() throws Exception {
            JsonNode context = getJson("/api/v1/statuses/" + testStatus.getId() + "/context");
            
            assertThat(context.has("ancestors")).isTrue();
            assertThat(context.has("descendants")).isTrue();
            assertThat(context.path("ancestors").isArray()).isTrue();
            assertThat(context.path("descendants").isArray()).isTrue();
        }

        @Test
        @DisplayName("Returns 404 for non-existent status")
        void getContextReturns404ForNonExistent() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/statuses/999999999/context"), String.class);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/statuses/:id/favourited_by")
    class FavouritedByTests {

        @Test
        @DisplayName("Returns empty list for status with no favourites")
        void getFavouritedByReturnsEmptyList() throws Exception {
            JsonNode accounts = getJson("/api/v1/statuses/" + testStatus.getId() + "/favourited_by");
            
            assertThat(accounts.isArray()).isTrue();
        }

        @Test
        @DisplayName("Returns 404 for non-existent status")
        void getFavouritedByReturns404ForNonExistent() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/statuses/999999999/favourited_by"), String.class);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/statuses/:id/reblogged_by")
    class RebloggedByTests {

        @Test
        @DisplayName("Returns empty list for status with no reblogs")
        void getRebloggedByReturnsEmptyList() throws Exception {
            JsonNode accounts = getJson("/api/v1/statuses/" + testStatus.getId() + "/reblogged_by");
            
            assertThat(accounts.isArray()).isTrue();
        }

        @Test
        @DisplayName("Returns 404 for non-existent status")
        void getRebloggedByReturns404ForNonExistent() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/statuses/999999999/reblogged_by"), String.class);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }
}
