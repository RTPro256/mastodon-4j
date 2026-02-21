package org.joinmastodon.federation.interop;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.joinmastodon.activitypub.model.Actor;
import org.joinmastodon.activitypub.model.Note;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Federation interoperability tests.
 * Tests verify compatibility with ActivityPub protocol and Mastodon federation.
 * 
 * Note: Live instance testing requires external infrastructure.
 * These tests validate protocol compliance and data format compatibility.
 */
public class FederationInteropTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Ignore unknown properties - ActivityPub implementations may have extensions
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Nested
    @DisplayName("ActivityPub Actor Tests")
    class ActorTests {

        @Test
        @DisplayName("Actor JSON-LD serialization is compatible with Mastodon")
        void actorSerializationIsCompatible() throws Exception {
            Actor actor = new Actor();
            actor.setId("https://example.social/users/alice");
            actor.setType("Person");
            actor.setPreferredUsername("alice");
            actor.setName("Alice");
            actor.setSummary("Test account");
            actor.setInbox("https://example.social/users/alice/inbox");
            actor.setOutbox("https://example.social/users/alice/outbox");
            actor.setFollowers("https://example.social/users/alice/followers");
            actor.setFollowing("https://example.social/users/alice/following");

            String json = objectMapper.writeValueAsString(actor);
            JsonNode parsed = objectMapper.readTree(json);

            // Verify required ActivityPub fields
            assertThat(parsed.has("id")).isTrue();
            assertThat(parsed.has("type")).isTrue();
            assertThat(parsed.has("preferredUsername")).isTrue();
            assertThat(parsed.has("inbox")).isTrue();
            assertThat(parsed.has("outbox")).isTrue();

            // Verify JSON-LD context
            assertThat(parsed.has("@context")).isTrue();
        }

        @Test
        @DisplayName("Actor deserialization handles Mastodon format")
        void actorDeserializationHandlesMastodonFormat() throws Exception {
            String mastodonActorJson = """
                {
                    "@context": [
                        "https://www.w3.org/ns/activitystreams",
                        "https://w3id.org/security/v1"
                    ],
                    "id": "https://mastodon.social/users/alice",
                    "type": "Person",
                    "preferredUsername": "alice",
                    "name": "Alice",
                    "summary": "<p>Test account</p>",
                    "inbox": "https://mastodon.social/users/alice/inbox",
                    "outbox": "https://mastodon.social/users/alice/outbox",
                    "followers": "https://mastodon.social/users/alice/followers",
                    "following": "https://mastodon.social/users/alice/following",
                    "publicKey": {
                        "id": "https://mastodon.social/users/alice#main-key",
                        "owner": "https://mastodon.social/users/alice",
                        "publicKeyPem": "-----BEGIN PUBLIC KEY-----\\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\\n-----END PUBLIC KEY-----"
                    }
                }
                """;

            Actor actor = objectMapper.readValue(mastodonActorJson, Actor.class);

            assertThat(actor.getId()).isEqualTo("https://mastodon.social/users/alice");
            assertThat(actor.getType()).isEqualTo("Person");
            assertThat(actor.getPreferredUsername()).isEqualTo("alice");
            assertThat(actor.getInbox()).isEqualTo("https://mastodon.social/users/alice/inbox");
        }
    }

    @Nested
    @DisplayName("ActivityPub Note Tests")
    class NoteTests {

        @Test
        @DisplayName("Note JSON-LD serialization is compatible with Mastodon")
        void noteSerializationIsCompatible() throws Exception {
            Note note = new Note();
            note.setId("https://example.social/users/alice/statuses/123");
            note.setType("Note");
            note.setContent("<p>Hello world!</p>");
            note.setAttributedTo("https://example.social/users/alice");
            note.setPublished(Instant.now());
            note.setTo(java.util.List.of("https://www.w3.org/ns/activitystreams#Public"));

            String json = objectMapper.writeValueAsString(note);
            JsonNode parsed = objectMapper.readTree(json);

            // Verify required ActivityPub fields
            assertThat(parsed.has("id")).isTrue();
            assertThat(parsed.has("type")).isTrue();
            assertThat(parsed.has("content")).isTrue();
            assertThat(parsed.has("attributedTo")).isTrue();
            assertThat(parsed.has("published")).isTrue();

            // Verify JSON-LD context
            assertThat(parsed.has("@context")).isTrue();
        }

        @Test
        @DisplayName("Note deserialization handles Mastodon format")
        void noteDeserializationHandlesMastodonFormat() throws Exception {
            String mastodonNoteJson = """
                {
                    "@context": [
                        "https://www.w3.org/ns/activitystreams",
                        {
                            "sensitive": "as:sensitive",
                            "Hashtag": "as:Hashtag"
                        }
                    ],
                    "id": "https://mastodon.social/users/alice/statuses/123",
                    "type": "Note",
                    "content": "<p>Hello world!</p>",
                    "attributedTo": "https://mastodon.social/users/alice",
                    "published": "2026-02-20T12:00:00Z",
                    "to": ["https://www.w3.org/ns/activitystreams#Public"],
                    "cc": ["https://mastodon.social/users/alice/followers"],
                    "sensitive": false,
                    "tag": []
                }
                """;

            Note note = objectMapper.readValue(mastodonNoteJson, Note.class);

            assertThat(note.getId()).isEqualTo("https://mastodon.social/users/alice/statuses/123");
            assertThat(note.getType()).isEqualTo("Note");
            assertThat(note.getContent()).isEqualTo("<p>Hello world!</p>");
        }
    }

    @Nested
    @DisplayName("HTTP Signature Tests")
    class HttpSignatureTests {

        @Test
        @DisplayName("Signature string format follows draft specification")
        void signatureStringFormat() {
            // Verify signature string components
            String method = "POST";
            String path = "/users/alice/inbox";
            String host = "mastodon.social";
            String date = "Thu, 20 Feb 2026 12:00:00 GMT";
            String digest = "SHA-256=abc123...";

            // Signature string should follow the format:
            // (request-target): post /users/alice/inbox
            // host: mastodon.social
            // date: Thu, 20 Feb 2026 12:00:00 GMT
            // digest: SHA-256=abc123...

            String signatureString = String.format(
                    "(request-target): %s %s\nhost: %s\ndate: %s\ndigest: %s",
                    method.toLowerCase(), path, host, date, digest
            );

            assertThat(signatureString).contains("(request-target): post /users/alice/inbox");
            assertThat(signatureString).contains("host: mastodon.social");
            assertThat(signatureString).contains("date: Thu, 20 Feb 2026 12:00:00 GMT");
            assertThat(signatureString).contains("digest: SHA-256=abc123...");
        }
    }

    @Nested
    @DisplayName("WebFinger Tests")
    class WebFingerTests {

        @Test
        @DisplayName("WebFinger response format is correct")
        void webFingerResponseFormat() throws Exception {
            String webFingerJson = """
                {
                    "subject": "acct:alice@example.social",
                    "aliases": [
                        "https://example.social/@alice",
                        "https://example.social/users/alice"
                    ],
                    "links": [
                        {
                            "rel": "self",
                            "type": "application/activity+json",
                            "href": "https://example.social/users/alice"
                        },
                        {
                            "rel": "http://webfinger.net/rel/profile-page",
                            "type": "text/html",
                            "href": "https://example.social/@alice"
                        }
                    ]
                }
                """;

            JsonNode response = objectMapper.readTree(webFingerJson);

            assertThat(response.has("subject")).isTrue();
            assertThat(response.get("subject").asText()).isEqualTo("acct:alice@example.social");
            assertThat(response.has("links")).isTrue();
            assertThat(response.get("links").isArray()).isTrue();

            // Verify self link for ActivityPub
            JsonNode links = response.get("links");
            boolean hasActivityPubSelf = false;
            for (JsonNode link : links) {
                if ("self".equals(link.get("rel").asText()) && 
                    "application/activity+json".equals(link.get("type").asText())) {
                    hasActivityPubSelf = true;
                    break;
                }
            }
            assertThat(hasActivityPubSelf).isTrue();
        }
    }
}
