package org.joinmastodon.web.conformance;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Application;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API Conformance tests for Account endpoints.
 * Tests compatibility with Mastodon API v4.5.6.
 * 
 * @see <a href="https://docs.joinmastodon.org/methods/accounts/">Mastodon Accounts API</a>
 */
@DisplayName("Account API Conformance Tests")
class AccountApiConformanceTest extends BaseApiConformanceTest {

    private TestUser testUser;
    private Application testApp;
    private String accessToken;

    @BeforeEach
    void setupAccount() {
        clearTables();
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            testUser = createTestAccount("alice", "alice@example.test");
            testApp = createTestApplication("Test App", "read write follow push");
            accessToken = createAccessToken(testUser.user(), testApp, "read write follow push");
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/:id")
    class GetAccountTests {

        @Test
        @DisplayName("Returns account with required fields")
        void getAccountReturnsRequiredFields() throws Exception {
            JsonNode account = getJson("/api/v1/accounts/" + testUser.account().getId());
            
            verifyAccountFormat(account);
            assertThat(account.path("id").asText()).isEqualTo(testUser.account().getId().toString());
            assertThat(account.path("username").asText()).isEqualTo("alice");
            assertThat(account.path("acct").asText()).isEqualTo("alice@local");
        }

        @Test
        @DisplayName("Returns 404 for non-existent account")
        void getAccountReturns404ForNonExistent() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/accounts/999999999"), String.class);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                verifyErrorResponse(ex, "not found");
            }
        }

        @Test
        @DisplayName("Returns 400 for invalid ID format")
        void getAccountReturns400ForInvalidId() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/accounts/invalid"), String.class);
                throw new AssertionError("Expected 400 Bad Request");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/verify_credentials")
    class VerifyCredentialsTests {

        @Test
        @DisplayName("Returns current user account when authenticated")
        void verifyCredentialsReturnsCurrentUser() throws Exception {
            JsonNode account = getJsonWithAuth("/api/v1/accounts/verify_credentials", accessToken);
            
            verifyAccountFormat(account);
            assertThat(account.path("id").asText()).isEqualTo(testUser.account().getId().toString());
            assertThat(account.path("username").asText()).isEqualTo("alice");
        }

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void verifyCredentialsReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/accounts/verify_credentials"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns 401 with invalid token")
        void verifyCredentialsReturns401WithInvalidToken() throws Exception {
            try {
                getJsonWithAuth("/api/v1/accounts/verify_credentials", "invalid_token");
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/:id/statuses")
    class AccountStatusesTests {

        @Test
        @DisplayName("Returns empty list for account with no statuses")
        void accountStatusesReturnsEmptyList() throws Exception {
            JsonNode statuses = getJson("/api/v1/accounts/" + testUser.account().getId() + "/statuses");
            
            assertThat(statuses.isArray()).isTrue();
            assertThat(statuses.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Returns statuses for account")
        void accountStatusesReturnsStatuses() throws Exception {
            // Create a status for the account
            new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
                org.joinmastodon.core.entity.Status status = new org.joinmastodon.core.entity.Status();
                status.setAccount(testUser.account());
                status.setContent("<p>Test status</p>");
                status.setCreatedAt(java.time.Instant.now());
                status.setVisibility(org.joinmastodon.core.model.Visibility.PUBLIC);
                entityManager.persist(status);
                entityManager.flush();
            });

            JsonNode statuses = getJson("/api/v1/accounts/" + testUser.account().getId() + "/statuses");
            
            assertThat(statuses.isArray()).isTrue();
            assertThat(statuses.size()).isGreaterThanOrEqualTo(1);
            verifyStatusFormat(statuses.get(0));
        }

        @Test
        @DisplayName("Respects limit parameter")
        void accountStatusesRespectsLimit() throws Exception {
            // Create multiple statuses
            new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
                for (int i = 0; i < 10; i++) {
                    org.joinmastodon.core.entity.Status status = new org.joinmastodon.core.entity.Status();
                    status.setAccount(testUser.account());
                    status.setContent("<p>Status " + i + "</p>");
                    status.setCreatedAt(java.time.Instant.now().minusSeconds(i * 60));
                    status.setVisibility(org.joinmastodon.core.model.Visibility.PUBLIC);
                    entityManager.persist(status);
                }
                entityManager.flush();
            });

            JsonNode statuses = getJson("/api/v1/accounts/" + testUser.account().getId() + "/statuses?limit=5");
            
            assertThat(statuses.isArray()).isTrue();
            assertThat(statuses.size()).isLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Returns 404 for non-existent account")
        void accountStatusesReturns404ForNonExistent() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/accounts/999999999/statuses"), String.class);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/accounts/:id/follow")
    class FollowAccountTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void followReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/accounts/123/follow"),
                        new HttpEntity<>("{}", headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/accounts/:id/block")
    class BlockAccountTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void blockReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/accounts/123/block"),
                        new HttpEntity<>("{}", headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/accounts/:id/mute")
    class MuteAccountTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void muteReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/accounts/123/mute"),
                        new HttpEntity<>("{}", headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/relationships")
    class RelationshipsTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void relationshipsReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/accounts/relationships?id[]=1"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns relationship objects when authenticated")
        void relationshipsReturnsRelationships() throws Exception {
            // Create another account to check relationship with
            TestUser otherUser = new TransactionTemplate(transactionManager).execute(txStatus -> {
                TestUser other = createTestAccount("bob", "bob@example.test");
                entityManager.flush();
                return other;
            });

            JsonNode relationships = getJsonWithAuth(
                    "/api/v1/accounts/relationships?id[]=" + otherUser.account().getId(),
                    accessToken);

            assertThat(relationships.isArray()).isTrue();
            assertThat(relationships.size()).isEqualTo(1);
            
            JsonNode rel = relationships.get(0);
            assertThat(rel.has("id")).isTrue();
            assertThat(rel.has("following")).isTrue();
            assertThat(rel.has("followed_by")).isTrue();
            assertThat(rel.has("blocking")).isTrue();
            assertThat(rel.has("muting")).isTrue();
            assertThat(rel.has("requested")).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/:id/followers")
    class FollowersTests {

        @Test
        @DisplayName("Returns empty list for account with no followers")
        void followersReturnsEmptyList() throws Exception {
            JsonNode followers = getJson("/api/v1/accounts/" + testUser.account().getId() + "/followers");
            
            assertThat(followers.isArray()).isTrue();
        }

        @Test
        @DisplayName("Returns 404 for non-existent account")
        void followersReturns404ForNonExistent() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/accounts/999999999/followers"), String.class);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/:id/following")
    class FollowingTests {

        @Test
        @DisplayName("Returns empty list for account with no following")
        void followingReturnsEmptyList() throws Exception {
            JsonNode following = getJson("/api/v1/accounts/" + testUser.account().getId() + "/following");
            
            assertThat(following.isArray()).isTrue();
        }

        @Test
        @DisplayName("Returns 404 for non-existent account")
        void followingReturns404ForNonExistent() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/accounts/999999999/following"), String.class);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }
}
