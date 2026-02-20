package org.joinmastodon.web.conformance;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API Conformance tests for Timeline endpoints.
 * Tests compatibility with Mastodon API v4.5.6.
 * 
 * @see <a href="https://docs.joinmastodon.org/methods/timelines/">Mastodon Timelines API</a>
 */
@DisplayName("Timeline API Conformance Tests")
class TimelineApiConformanceTest extends BaseApiConformanceTest {

    private TestUser testUser;
    private Application testApp;
    private String accessToken;

    @BeforeEach
    void setupTimeline() {
        clearTables();
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            testUser = createTestAccount("alice", "alice@example.test");
            testApp = createTestApplication("Test App", "read");
            accessToken = createAccessToken(testUser.user(), testApp, "read");
            
            // Create some test statuses
            for (int i = 0; i < 5; i++) {
                Status status = new Status();
                status.setAccount(testUser.account());
                status.setContent("<p>Timeline status " + i + "</p>");
                status.setCreatedAt(Instant.now().minusSeconds(i * 60));
                status.setVisibility(Visibility.PUBLIC);
                entityManager.persist(status);
            }
            
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("GET /api/v1/timelines/home")
    class HomeTimelineTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void homeTimelineReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/timelines/home"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns statuses when authenticated")
        void homeTimelineReturnsStatuses() throws Exception {
            JsonNode statuses = getJsonWithAuth("/api/v1/timelines/home", accessToken);
            
            assertThat(statuses.isArray()).isTrue();
            assertThat(statuses.size()).isGreaterThanOrEqualTo(1);
            
            // Verify each status has required fields
            for (JsonNode status : statuses) {
                verifyStatusFormat(status);
            }
        }

        @Test
        @DisplayName("Respects limit parameter")
        void homeTimelineRespectsLimit() throws Exception {
            JsonNode statuses = getJsonWithAuth("/api/v1/timelines/home?limit=2", accessToken);
            
            assertThat(statuses.isArray()).isTrue();
            assertThat(statuses.size()).isLessThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Returns statuses in reverse chronological order")
        void homeTimelineReturnsChronologicalOrder() throws Exception {
            JsonNode statuses = getJsonWithAuth("/api/v1/timelines/home", accessToken);
            
            assertThat(statuses.isArray()).isTrue();
            if (statuses.size() > 1) {
                String firstId = statuses.get(0).path("id").asText();
                String secondId = statuses.get(1).path("id").asText();
                // IDs should be in descending order (newer first)
                assertThat(Long.parseLong(firstId)).isGreaterThan(Long.parseLong(secondId));
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/timelines/public")
    class PublicTimelineTests {

        @Test
        @DisplayName("Returns statuses without authentication")
        void publicTimelineReturnsStatusesWithoutAuth() throws Exception {
            JsonNode statuses = getJson("/api/v1/timelines/public");
            
            assertThat(statuses.isArray()).isTrue();
            assertThat(statuses.size()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Respects limit parameter")
        void publicTimelineRespectsLimit() throws Exception {
            JsonNode statuses = getJson("/api/v1/timelines/public?limit=3");
            
            assertThat(statuses.isArray()).isTrue();
            assertThat(statuses.size()).isLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Only returns public statuses")
        void publicTimelineOnlyReturnsPublicStatuses() throws Exception {
            // Create a private status
            new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
                Status privateStatus = new Status();
                privateStatus.setAccount(testUser.account());
                privateStatus.setContent("<p>Private status</p>");
                privateStatus.setCreatedAt(Instant.now());
                privateStatus.setVisibility(Visibility.PRIVATE);
                entityManager.persist(privateStatus);
                entityManager.flush();
            });

            JsonNode statuses = getJson("/api/v1/timelines/public");
            
            for (JsonNode status : statuses) {
                assertThat(status.path("visibility").asText()).isEqualTo("public");
            }
        }

        @Test
        @DisplayName("Supports pagination with max_id")
        void publicTimelineSupportsMaxId() throws Exception {
            JsonNode firstPage = getJson("/api/v1/timelines/public?limit=2");
            
            if (firstPage.size() > 0) {
                String lastId = firstPage.get(firstPage.size() - 1).path("id").asText();
                JsonNode secondPage = getJson("/api/v1/timelines/public?limit=2&max_id=" + lastId);
                
                // Verify no overlap
                for (JsonNode status : secondPage) {
                    assertThat(status.path("id").asText()).isNotEqualTo(lastId);
                }
            }
        }

        @Test
        @DisplayName("Supports pagination with since_id")
        void publicTimelineSupportsSinceId() throws Exception {
            JsonNode firstPage = getJson("/api/v1/timelines/public?limit=2");
            
            if (firstPage.size() > 0) {
                String firstId = firstPage.get(0).path("id").asText();
                JsonNode newPage = getJson("/api/v1/timelines/public?since_id=" + firstId);
                
                // All returned statuses should be newer than since_id
                for (JsonNode status : newPage) {
                    assertThat(Long.parseLong(status.path("id").asText()))
                            .isGreaterThan(Long.parseLong(firstId));
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/timelines/tag/:hashtag")
    class HashtagTimelineTests {

        @BeforeEach
        void setupHashtagStatuses() {
            new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
                // Create status with hashtag
                Status hashtagStatus = new Status();
                hashtagStatus.setAccount(testUser.account());
                hashtagStatus.setContent("<p>Check out #testtag!</p>");
                hashtagStatus.setCreatedAt(Instant.now());
                hashtagStatus.setVisibility(Visibility.PUBLIC);
                entityManager.persist(hashtagStatus);
                entityManager.flush();
            });
        }

        @Test
        @DisplayName("Returns statuses without authentication")
        void hashtagTimelineReturnsStatusesWithoutAuth() throws Exception {
            JsonNode statuses = getJson("/api/v1/timelines/tag/testtag");
            
            assertThat(statuses.isArray()).isTrue();
        }

        @Test
        @DisplayName("Respects limit parameter")
        void hashtagTimelineRespectsLimit() throws Exception {
            JsonNode statuses = getJson("/api/v1/timelines/tag/testtag?limit=1");
            
            assertThat(statuses.isArray()).isTrue();
            assertThat(statuses.size()).isLessThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/timelines/list/:id")
    class ListTimelineTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void listTimelineReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/timelines/list/1"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns 404 for non-existent list")
        void listTimelineReturns404ForNonExistent() throws Exception {
            try {
                getJsonWithAuth("/api/v1/timelines/list/999999999", accessToken);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/conversations")
    class ConversationsTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void conversationsReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/conversations"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns conversations when authenticated")
        void conversationsReturnsConversations() throws Exception {
            JsonNode conversations = getJsonWithAuth("/api/v1/conversations", accessToken);
            
            assertThat(conversations.isArray()).isTrue();
        }
    }

    @Nested
    @DisplayName("Link Header Pagination")
    class PaginationTests {

        @Test
        @DisplayName("Public timeline includes Link header for pagination")
        void publicTimelineIncludesLinkHeader() throws Exception {
            var response = restTemplate.getForEntity(baseUrl("/api/v1/timelines/public?limit=2"), String.class);
            
            HttpHeaders headers = response.getHeaders();
            assertThat(headers.get("Link")).isNotNull();
            
            String linkHeader = headers.getFirst("Link");
            assertThat(linkHeader).isNotNull();
            // Link header should contain next/prev links
            assertThat(linkHeader).containsAnyOf("next", "prev");
        }
    }
}
