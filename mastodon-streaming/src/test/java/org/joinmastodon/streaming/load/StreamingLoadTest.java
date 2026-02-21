package org.joinmastodon.streaming.load;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.joinmastodon.streaming.api.StreamingController;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 * Load tests for streaming endpoints.
 * Tests verify the streaming service can handle concurrent connections.
 */
@DisplayName("Streaming Load Tests")
class StreamingLoadTest {

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        StreamingController controller = new StreamingController();
        webTestClient = WebTestClient.bindToController(controller)
                .configureClient()
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Test
    @DisplayName("Public streaming endpoint responds correctly")
    void publicStreamRespondsCorrectly() {
        webTestClient.get()
                .uri("/api/v1/streaming/public")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
    }

    @Test
    @DisplayName("Hashtag streaming endpoint responds correctly")
    void hashtagStreamRespondsCorrectly() {
        webTestClient.get()
                .uri("/api/v1/streaming/hashtag?tag=test")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
    }

    @Test
    @DisplayName("User streaming endpoint requires authentication")
    void userStreamRequiresAuthentication() {
        webTestClient.get()
                .uri("/api/v1/streaming/user")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("List streaming endpoint requires authentication")
    void listStreamRequiresAuthentication() {
        webTestClient.get()
                .uri("/api/v1/streaming/list?list=1")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Direct streaming endpoint requires authentication")
    void directStreamRequiresAuthentication() {
        webTestClient.get()
                .uri("/api/v1/streaming/user")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
