package org.joinmastodon.web.conformance;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.MediaAttachment;
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
 * API Conformance tests for Media endpoints.
 * Tests compatibility with Mastodon API v4.5.6.
 * 
 * @see <a href="https://docs.joinmastodon.org/methods/media/">Mastodon Media API</a>
 */
@DisplayName("Media API Conformance Tests")
class MediaApiConformanceTest extends BaseApiConformanceTest {

    private TestUser testUser;
    private Application testApp;
    private String accessToken;
    private MediaAttachment testMedia;

    @BeforeEach
    void setupMedia() {
        clearTables();
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            testUser = createTestAccount("alice", "alice@example.test");
            testApp = createTestApplication("Test App", "read write");
            accessToken = createAccessToken(testUser.user(), testApp, "read write");
            
            testMedia = new MediaAttachment();
            testMedia.setAccountId(testUser.account().getId());
            testMedia.setType("image");
            testMedia.setUrl("https://example.test/media/test.png");
            testMedia.setPreviewUrl("https://example.test/media/test-preview.png");
            testMedia.setDescription("Test image");
            testMedia.setCreatedAt(Instant.now());
            entityManager.persist(testMedia);
            
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("GET /api/v1/media/:id")
    class GetMediaTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void getMediaReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(
                        baseUrl("/api/v1/media/" + testMedia.getId()),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns media when authenticated")
        void getMediaReturnsMedia() throws Exception {
            JsonNode media = getJsonWithAuth(
                    "/api/v1/media/" + testMedia.getId(),
                    accessToken);
            
            verifyMediaFormat(media);
            assertThat(media.path("id").asText()).isEqualTo(testMedia.getId().toString());
        }

        @Test
        @DisplayName("Returns 404 for non-existent media")
        void getMediaReturns404ForNonExistent() throws Exception {
            try {
                getJsonWithAuth("/api/v1/media/999999999", accessToken);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/media/:id")
    class UpdateMediaTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void updateMediaReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String payload = """
                {
                  "description": "Updated description"
                }
                """;
            
            try {
                restTemplate.exchange(
                        baseUrl("/api/v1/media/" + testMedia.getId()),
                        HttpMethod.PUT,
                        new HttpEntity<>(payload, headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Updates media description when authenticated")
        void updateMediaDescriptionWhenAuthenticated() throws Exception {
            String payload = """
                {
                  "description": "Updated test image description"
                }
                """;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            var response = restTemplate.exchange(
                    baseUrl("/api/v1/media/" + testMedia.getId()),
                    HttpMethod.PUT,
                    new HttpEntity<>(payload, headers),
                    String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode media = objectMapper.readTree(response.getBody());
            assertThat(media.path("description").asText())
                    .isEqualTo("Updated test image description");
        }
    }

    @Nested
    @DisplayName("Media Response Format")
    class MediaFormatTests {

        @Test
        @DisplayName("Media has required fields")
        void mediaHasRequiredFields() throws Exception {
            JsonNode media = getJsonWithAuth(
                    "/api/v1/media/" + testMedia.getId(),
                    accessToken);
            
            verifyMediaFormat(media);
        }

        @Test
        @DisplayName("Media has valid type")
        void mediaHasValidType() throws Exception {
            JsonNode media = getJsonWithAuth(
                    "/api/v1/media/" + testMedia.getId(),
                    accessToken);
            
            String type = media.path("type").asText();
            assertThat(type).isIn("unknown", "image", "gifv", "video", "audio");
        }

        @Test
        @DisplayName("Media includes meta information")
        void mediaIncludesMeta() throws Exception {
            JsonNode media = getJsonWithAuth(
                    "/api/v1/media/" + testMedia.getId(),
                    accessToken);
            
            // Meta field should exist (may be null for some media types)
            assertThat(media.has("meta")).isTrue();
        }
    }

    @Nested
    @DisplayName("POST /api/v2/media")
    class UploadMediaTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void uploadMediaReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v2/media"),
                        new HttpEntity<>(null, headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }
    }
}
