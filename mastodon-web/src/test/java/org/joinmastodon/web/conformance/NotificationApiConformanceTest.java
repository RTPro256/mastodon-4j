package org.joinmastodon.web.conformance;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.Notification;
import org.joinmastodon.core.entity.NotificationType;
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
 * API Conformance tests for Notification endpoints.
 * Tests compatibility with Mastodon API v4.5.6.
 * 
 * @see <a href="https://docs.joinmastodon.org/methods/notifications/">Mastodon Notifications API</a>
 */
@DisplayName("Notification API Conformance Tests")
class NotificationApiConformanceTest extends BaseApiConformanceTest {

    private TestUser testUser;
    private TestUser otherUser;
    private Application testApp;
    private String accessToken;
    private Notification testNotification;

    @BeforeEach
    void setupNotifications() {
        clearTables();
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            testUser = createTestAccount("alice", "alice@example.test");
            otherUser = createTestAccount("bob", "bob@example.test");
            testApp = createTestApplication("Test App", "read write");
            accessToken = createAccessToken(testUser.user(), testApp, "read write");
            
            // Create a status for the notification
            Status status = new Status();
            status.setAccount(otherUser.account());
            status.setContent("<p>Test status</p>");
            status.setCreatedAt(Instant.now());
            status.setVisibility(Visibility.PUBLIC);
            entityManager.persist(status);
            
            // Create a notification
            testNotification = new Notification();
            testNotification.setAccount(testUser.account());
            testNotification.setActor(otherUser.account());
            testNotification.setType(NotificationType.FAVOURITE);
            testNotification.setStatus(status);
            testNotification.setCreatedAt(Instant.now());
            entityManager.persist(testNotification);
            
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("GET /api/v1/notifications")
    class GetNotificationsTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void getNotificationsReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/notifications"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns notifications when authenticated")
        void getNotificationsReturnsNotifications() throws Exception {
            JsonNode notifications = getJsonWithAuth("/api/v1/notifications", accessToken);
            
            assertThat(notifications.isArray()).isTrue();
            assertThat(notifications.size()).isGreaterThanOrEqualTo(1);
            
            for (JsonNode notification : notifications) {
                verifyNotificationFormat(notification);
            }
        }

        @Test
        @DisplayName("Respects limit parameter")
        void getNotificationsRespectsLimit() throws Exception {
            JsonNode notifications = getJsonWithAuth("/api/v1/notifications?limit=1", accessToken);
            
            assertThat(notifications.isArray()).isTrue();
            assertThat(notifications.size()).isLessThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Filters by notification type")
        void getNotificationsFiltersByType() throws Exception {
            JsonNode notifications = getJsonWithAuth(
                    "/api/v1/notifications?types[]=favourite",
                    accessToken);
            
            for (JsonNode notification : notifications) {
                assertThat(notification.path("type").asText()).isEqualTo("favourite");
            }
        }

        @Test
        @DisplayName("Excludes notification types")
        void getNotificationsExcludesTypes() throws Exception {
            JsonNode notifications = getJsonWithAuth(
                    "/api/v1/notifications?exclude_types[]=follow",
                    accessToken);
            
            for (JsonNode notification : notifications) {
                assertThat(notification.path("type").asText()).isNotEqualTo("follow");
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/notifications/:id")
    class GetNotificationTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void getNotificationReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(
                        baseUrl("/api/v1/notifications/" + testNotification.getId()),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns notification when authenticated")
        void getNotificationReturnsNotification() throws Exception {
            JsonNode notification = getJsonWithAuth(
                    "/api/v1/notifications/" + testNotification.getId(),
                    accessToken);
            
            verifyNotificationFormat(notification);
            assertThat(notification.path("id").asText())
                    .isEqualTo(testNotification.getId().toString());
        }

        @Test
        @DisplayName("Returns 404 for non-existent notification")
        void getNotificationReturns404ForNonExistent() throws Exception {
            try {
                getJsonWithAuth("/api/v1/notifications/999999999", accessToken);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/notifications/clear")
    class ClearNotificationsTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void clearNotificationsReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/notifications/clear"),
                        null,
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Clears all notifications when authenticated")
        void clearNotificationsWhenAuthenticated() throws Exception {
            postJsonWithAuth("/api/v1/notifications/clear", "{}", accessToken);
            
            // Verify notifications are cleared
            JsonNode notifications = getJsonWithAuth("/api/v1/notifications", accessToken);
            assertThat(notifications.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/notifications/:id/dismiss")
    class DismissNotificationTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void dismissNotificationReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/notifications/" + testNotification.getId() + "/dismiss"),
                        null,
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Dismisses notification when authenticated")
        void dismissNotificationWhenAuthenticated() throws Exception {
            postJsonWithAuth(
                    "/api/v1/notifications/" + testNotification.getId() + "/dismiss",
                    "{}",
                    accessToken);
            
            // Verify notification is dismissed
            try {
                getJsonWithAuth(
                        "/api/v1/notifications/" + testNotification.getId(),
                        accessToken);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("Notification Response Format")
    class NotificationFormatTests {

        @Test
        @DisplayName("Notification includes account")
        void notificationIncludesAccount() throws Exception {
            JsonNode notification = getJsonWithAuth(
                    "/api/v1/notifications/" + testNotification.getId(),
                    accessToken);
            
            JsonNode account = notification.path("account");
            verifyAccountFormat(account);
        }

        @Test
        @DisplayName("Notification includes status when applicable")
        void notificationIncludesStatus() throws Exception {
            JsonNode notification = getJsonWithAuth(
                    "/api/v1/notifications/" + testNotification.getId(),
                    accessToken);
            
            // Favourite notifications should include the status
            assertThat(notification.has("status")).isTrue();
            if (!notification.path("status").isNull()) {
                verifyStatusFormat(notification.path("status"));
            }
        }

        @Test
        @DisplayName("Notification has valid type")
        void notificationHasValidType() throws Exception {
            JsonNode notification = getJsonWithAuth(
                    "/api/v1/notifications/" + testNotification.getId(),
                    accessToken);
            
            String type = notification.path("type").asText();
            assertThat(type).isIn("mention", "status", "reblog", "follow", "follow_request",
                    "favourite", "poll", "update", "admin.sign_up", "admin.report");
        }

        @Test
        @DisplayName("Notification has created_at in ISO format")
        void notificationHasValidCreatedAt() throws Exception {
            JsonNode notification = getJsonWithAuth(
                    "/api/v1/notifications/" + testNotification.getId(),
                    accessToken);
            
            String createdAt = notification.path("created_at").asText();
            assertThat(createdAt).isNotEmpty();
            // Should be parseable as ISO instant
            Instant.parse(createdAt);
        }
    }
}
