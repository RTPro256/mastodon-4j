package org.joinmastodon.streaming;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.joinmastodon.streaming.api.StreamingController;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 * Tests for streaming API endpoints.
 * Tests authentication and endpoint availability.
 */
@DisplayName("Streaming Connection Tests")
class StreamingConnectionLoadTest {

    private WebTestClient createTestClient() {
        StreamingController controller = new StreamingController();
        return WebTestClient.bindToController(controller)
                .configureClient()
                .responseTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Nested
    @DisplayName("Authentication")
    class AuthenticationTests {

        @Test
        @DisplayName("User stream requires authentication")
        void userStreamRequiresAuthentication() {
            createTestClient().get()
                    .uri("/api/v1/streaming/user")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("List stream requires authentication")
        void listStreamRequiresAuthentication() {
            createTestClient().get()
                    .uri("/api/v1/streaming/list?list=1")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }
}
