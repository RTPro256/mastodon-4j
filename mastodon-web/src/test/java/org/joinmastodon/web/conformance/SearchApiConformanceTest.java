package org.joinmastodon.web.conformance;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API Conformance tests for Search endpoints.
 * Tests compatibility with Mastodon API v4.5.6.
 * 
 * @see <a href="https://docs.joinmastodon.org/methods/search/">Mastodon Search API</a>
 */
@DisplayName("Search API Conformance Tests")
class SearchApiConformanceTest extends BaseApiConformanceTest {

    private TestUser testUser;
    private Application testApp;
    private String accessToken;

    @BeforeEach
    void setupSearch() {
        clearTables();
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            testUser = createTestAccount("alice", "alice@example.test");
            testApp = createTestApplication("Test App", "read");
            accessToken = createAccessToken(testUser.user(), testApp, "read");
            
            // Create some searchable content
            Status status = new Status();
            status.setAccount(testUser.account());
            status.setContent("<p>Searchable content about #testing</p>");
            status.setCreatedAt(Instant.now());
            status.setVisibility(Visibility.PUBLIC);
            entityManager.persist(status);
            
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("GET /api/v2/search")
    class SearchV2Tests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void searchReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v2/search?q=test"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns search results when authenticated")
        void searchReturnsResultsWhenAuthenticated() throws Exception {
            JsonNode results = getJsonWithAuth("/api/v2/search?q=testing", accessToken);
            
            assertThat(results.has("accounts")).isTrue();
            assertThat(results.has("statuses")).isTrue();
            assertThat(results.has("hashtags")).isTrue();
            
            assertThat(results.path("accounts").isArray()).isTrue();
            assertThat(results.path("statuses").isArray()).isTrue();
            assertThat(results.path("hashtags").isArray()).isTrue();
        }

        @Test
        @DisplayName("Respects type parameter")
        void searchRespectsTypeParameter() throws Exception {
            JsonNode results = getJsonWithAuth("/api/v2/search?q=testing&type=statuses", accessToken);
            
            // Should only return statuses
            assertThat(results.path("accounts").size()).isEqualTo(0);
            assertThat(results.path("hashtags").size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Respects limit parameter")
        void searchRespectsLimit() throws Exception {
            JsonNode results = getJsonWithAuth("/api/v2/search?q=test&limit=5", accessToken);
            
            assertThat(results.path("accounts").size()).isLessThanOrEqualTo(5);
            assertThat(results.path("statuses").size()).isLessThanOrEqualTo(5);
            assertThat(results.path("hashtags").size()).isLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Resolves accounts when resolve=true")
        void searchResolvesAccounts() throws Exception {
            JsonNode results = getJsonWithAuth("/api/v2/search?q=alice&resolve=true", accessToken);
            
            assertThat(results.path("accounts").isArray()).isTrue();
        }

        @Test
        @DisplayName("Returns 400 for missing q parameter")
        void searchReturns400ForMissingQuery() throws Exception {
            try {
                getJsonWithAuth("/api/v2/search", accessToken);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Nested
    @DisplayName("Search Result Format")
    class SearchResultFormatTests {

        @Test
        @DisplayName("Account results have required fields")
        void accountResultsHaveRequiredFields() throws Exception {
            JsonNode results = getJsonWithAuth("/api/v2/search?q=alice", accessToken);
            
            JsonNode accounts = results.path("accounts");
            for (JsonNode account : accounts) {
                verifyAccountFormat(account);
            }
        }

        @Test
        @DisplayName("Status results have required fields")
        void statusResultsHaveRequiredFields() throws Exception {
            JsonNode results = getJsonWithAuth("/api/v2/search?q=testing", accessToken);
            
            JsonNode statuses = results.path("statuses");
            for (JsonNode status : statuses) {
                verifyStatusFormat(status);
            }
        }

        @Test
        @DisplayName("Hashtag results have required fields")
        void hashtagResultsHaveRequiredFields() throws Exception {
            JsonNode results = getJsonWithAuth("/api/v2/search?q=testing", accessToken);
            
            JsonNode hashtags = results.path("hashtags");
            for (JsonNode hashtag : hashtags) {
                assertThat(hashtag.has("name")).isTrue();
                assertThat(hashtag.has("url")).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/search")
    class SearchV1Tests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void searchV1Returns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/search?q=test"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns search results when authenticated")
        void searchV1ReturnsResultsWhenAuthenticated() throws Exception {
            JsonNode results = getJsonWithAuth("/api/v1/search?q=testing", accessToken);
            
            // V1 search returns results in a different format
            assertThat(results.has("accounts")).isTrue();
            assertThat(results.has("statuses")).isTrue();
            assertThat(results.has("hashtags")).isTrue();
        }
    }

    @Nested
    @DisplayName("Pagination")
    class SearchPaginationTests {

        @Test
        @DisplayName("Supports offset parameter")
        void searchSupportsOffset() throws Exception {
            JsonNode firstPage = getJsonWithAuth("/api/v2/search?q=test&limit=2&offset=0", accessToken);
            JsonNode secondPage = getJsonWithAuth("/api/v2/search?q=test&limit=2&offset=2", accessToken);
            
            // Results should be different (if there are enough results)
            if (firstPage.path("accounts").size() > 0 && secondPage.path("accounts").size() > 0) {
                String firstId = firstPage.path("accounts").get(0).path("id").asText();
                String secondId = secondPage.path("accounts").get(0).path("id").asText();
                assertThat(firstId).isNotEqualTo(secondId);
            }
        }
    }
}
