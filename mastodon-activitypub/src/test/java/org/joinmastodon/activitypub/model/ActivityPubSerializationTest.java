package org.joinmastodon.activitypub.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ActivityPub JSON-LD serialization.
 * Verifies that serialized objects match the ActivityStreams 2.0 specification
 * and are compatible with Mastodon's expected format.
 * 
 * @see <a href="https://www.w3.org/TR/activitystreams-core/">ActivityStreams 2.0</a>
 * @see <a href="https://www.w3.org/TR/activitypub/">ActivityPub</a>
 */
@DisplayName("ActivityPub Serialization Tests")
class ActivityPubSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    @DisplayName("Actor Serialization")
    class ActorSerializationTests {

        @Test
        @DisplayName("Serializes Actor with required fields")
        void serializesActorWithRequiredFields() throws Exception {
            Actor actor = new Actor();
            actor.setId("https://example.com/users/alice");
            actor.setType("Person");
            actor.setPreferredUsername("alice");
            actor.setName("Alice");
            actor.setInbox("https://example.com/users/alice/inbox");
            actor.setOutbox("https://example.com/users/alice/outbox");
            actor.setFollowers("https://example.com/users/alice/followers");
            actor.setFollowing("https://example.com/users/alice/following");

            String json = objectMapper.writeValueAsString(actor);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("@context").asText()).isEqualTo("https://www.w3.org/ns/activitystreams");
            assertThat(node.get("id").asText()).isEqualTo("https://example.com/users/alice");
            assertThat(node.get("type").asText()).isEqualTo("Person");
            assertThat(node.get("preferredUsername").asText()).isEqualTo("alice");
            assertThat(node.get("inbox").asText()).isEqualTo("https://example.com/users/alice/inbox");
            assertThat(node.get("outbox").asText()).isEqualTo("https://example.com/users/alice/outbox");
        }

        @Test
        @DisplayName("Includes public key for verification")
        void includesPublicKey() throws Exception {
            Actor actor = new Actor();
            actor.setId("https://example.com/users/alice");
            actor.setType("Person");
            actor.setPreferredUsername("alice");

            PublicKey publicKey = new PublicKey();
            publicKey.setId("https://example.com/users/alice#main-key");
            publicKey.setOwner("https://example.com/users/alice");
            publicKey.setPublicKeyPem("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----");
            actor.setPublicKey(publicKey);

            String json = objectMapper.writeValueAsString(actor);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.has("publicKey")).isTrue();
            JsonNode keyNode = node.get("publicKey");
            assertThat(keyNode.get("id").asText()).isEqualTo("https://example.com/users/alice#main-key");
            assertThat(keyNode.get("owner").asText()).isEqualTo("https://example.com/users/alice");
            assertThat(keyNode.get("publicKeyPem").asText()).contains("BEGIN PUBLIC KEY");
        }

        @Test
        @DisplayName("Includes image/icon for avatar")
        void includesImageForAvatar() throws Exception {
            Actor actor = new Actor();
            actor.setId("https://example.com/users/alice");
            actor.setType("Person");
            actor.setPreferredUsername("alice");

            MediaLink icon = new MediaLink();
            icon.setType("Image");
            icon.setMediaType("image/png");
            icon.setUrl("https://example.com/users/alice/avatar.png");
            actor.setIcon(icon);

            String json = objectMapper.writeValueAsString(actor);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.has("icon")).isTrue();
            JsonNode iconNode = node.get("icon");
            assertThat(iconNode.get("type").asText()).isEqualTo("Image");
            assertThat(iconNode.get("url").asText()).isEqualTo("https://example.com/users/alice/avatar.png");
        }
    }

    @Nested
    @DisplayName("Note Serialization")
    class NoteSerializationTests {

        @Test
        @DisplayName("Serializes Note with required fields")
        void serializesNoteWithRequiredFields() throws Exception {
            Note note = new Note();
            note.setId("https://example.com/users/alice/statuses/123");
            note.setType("Note");
            note.setContent("<p>Hello world!</p>");
            note.setAttributedTo("https://example.com/users/alice");
            note.setPublished(Instant.parse("2024-02-20T12:00:00Z"));
            note.setTo(List.of("https://www.w3.org/ns/activitystreams#Public"));
            note.setCc(List.of("https://example.com/users/alice/followers"));

            String json = objectMapper.writeValueAsString(note);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("@context").asText()).isEqualTo("https://www.w3.org/ns/activitystreams");
            assertThat(node.get("id").asText()).isEqualTo("https://example.com/users/alice/statuses/123");
            assertThat(node.get("type").asText()).isEqualTo("Note");
            assertThat(node.get("content").asText()).isEqualTo("<p>Hello world!</p>");
            assertThat(node.get("attributedTo").asText()).isEqualTo("https://example.com/users/alice");
        }
    }

    @Nested
    @DisplayName("Activity Serialization")
    class ActivitySerializationTests {

        @Test
        @DisplayName("Serializes Create activity")
        void serializesCreateActivity() throws Exception {
            Note note = new Note();
            note.setId("https://example.com/users/alice/statuses/123");
            note.setType("Note");
            note.setContent("<p>Hello world!</p>");

            Create create = new Create();
            create.setId("https://example.com/users/alice/statuses/123/activity");
            create.setType("Create");
            create.setActor("https://example.com/users/alice");
            create.setObject(note);
            create.setTo(List.of("https://www.w3.org/ns/activitystreams#Public"));
            create.setCc(List.of("https://example.com/users/alice/followers"));

            String json = objectMapper.writeValueAsString(create);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("@context").asText()).isEqualTo("https://www.w3.org/ns/activitystreams");
            assertThat(node.get("type").asText()).isEqualTo("Create");
            assertThat(node.get("actor").asText()).isEqualTo("https://example.com/users/alice");
            assertThat(node.has("object")).isTrue();
            assertThat(node.get("object").get("type").asText()).isEqualTo("Note");
        }

        @Test
        @DisplayName("Serializes Follow activity")
        void serializesFollowActivity() throws Exception {
            Follow follow = new Follow();
            follow.setId("https://example.com/users/alice/follows/123");
            follow.setType("Follow");
            follow.setActor("https://example.com/users/alice");
            follow.setObject("https://example.com/users/bob");

            String json = objectMapper.writeValueAsString(follow);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("@context").asText()).isEqualTo("https://www.w3.org/ns/activitystreams");
            assertThat(node.get("type").asText()).isEqualTo("Follow");
            assertThat(node.get("actor").asText()).isEqualTo("https://example.com/users/alice");
            assertThat(node.get("object").asText()).isEqualTo("https://example.com/users/bob");
        }

        @Test
        @DisplayName("Serializes Like activity")
        void serializesLikeActivity() throws Exception {
            LikeActivity like = new LikeActivity();
            like.setId("https://example.com/users/alice/likes/123");
            like.setType("Like");
            like.setActor("https://example.com/users/alice");
            like.setObject("https://example.com/users/bob/statuses/456");

            String json = objectMapper.writeValueAsString(like);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("Like");
            assertThat(node.get("object").asText()).isEqualTo("https://example.com/users/bob/statuses/456");
        }

        @Test
        @DisplayName("Serializes Announce (boost) activity")
        void serializesAnnounceActivity() throws Exception {
            AnnounceActivity announce = new AnnounceActivity(
                    "https://example.com/users/alice",
                    "https://example.com/users/bob/statuses/456"
            );
            announce.setId("https://example.com/users/alice/announces/123");
            announce.setTo(List.of("https://www.w3.org/ns/activitystreams#Public"));
            announce.setCc(List.of("https://example.com/users/alice/followers", "https://example.com/users/bob"));

            String json = objectMapper.writeValueAsString(announce);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("Announce");
            assertThat(node.get("object").asText()).isEqualTo("https://example.com/users/bob/statuses/456");
        }

        @Test
        @DisplayName("Serializes Undo activity")
        void serializesUndoActivity() throws Exception {
            LikeActivity like = new LikeActivity();
            like.setId("https://example.com/users/alice/likes/123");
            like.setType("Like");
            like.setActor("https://example.com/users/alice");
            like.setObject("https://example.com/users/bob/statuses/456");

            Undo undo = new Undo();
            undo.setId("https://example.com/users/alice/undo/123");
            undo.setType("Undo");
            undo.setActor("https://example.com/users/alice");
            undo.setObject(like);

            String json = objectMapper.writeValueAsString(undo);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("Undo");
            assertThat(node.get("object").get("type").asText()).isEqualTo("Like");
        }

        @Test
        @DisplayName("Serializes Delete activity")
        void serializesDeleteActivity() throws Exception {
            DeleteActivity delete = new DeleteActivity(
                    "https://example.com/users/alice",
                    "https://example.com/users/alice/statuses/123"
            );
            delete.setId("https://example.com/users/alice/statuses/123#delete");

            String json = objectMapper.writeValueAsString(delete);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("Delete");
            assertThat(node.get("object").asText()).isEqualTo("https://example.com/users/alice/statuses/123");
        }
    }

    @Nested
    @DisplayName("Collection Serialization")
    class CollectionSerializationTests {

        @Test
        @DisplayName("Serializes OrderedCollection")
        void serializesOrderedCollection() throws Exception {
            OrderedCollection<String> collection = new OrderedCollection<>();
            collection.setId("https://example.com/users/alice/followers");
            collection.setType("OrderedCollection");
            collection.setTotalItems(42);

            String json = objectMapper.writeValueAsString(collection);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("OrderedCollection");
            assertThat(node.get("totalItems").asInt()).isEqualTo(42);
        }

        @Test
        @DisplayName("Serializes OrderedCollectionPage")
        void serializesOrderedCollectionPage() throws Exception {
            OrderedCollectionPage<String> page = new OrderedCollectionPage<>();
            page.setId("https://example.com/users/alice/followers?page=1");
            page.setType("OrderedCollectionPage");
            page.setPartOf("https://example.com/users/alice/followers");
            page.setOrderedItems(List.of("https://example.com/users/bob", "https://example.com/users/carol"));
            page.setNext("https://example.com/users/alice/followers?page=2");

            String json = objectMapper.writeValueAsString(page);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("OrderedCollectionPage");
            assertThat(node.get("partOf").asText()).isEqualTo("https://example.com/users/alice/followers");
            assertThat(node.get("orderedItems").isArray()).isTrue();
            assertThat(node.get("orderedItems").size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("JSON-LD Context")
    class JsonLdContextTests {

        @Test
        @DisplayName("Includes ActivityStreams context")
        void includesActivityStreamsContext() throws Exception {
            Note note = new Note();
            note.setId("https://example.com/notes/123");
            note.setType("Note");

            String json = objectMapper.writeValueAsString(note);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.has("@context")).isTrue();
            assertThat(node.get("@context").asText()).isEqualTo("https://www.w3.org/ns/activitystreams");
        }
    }
}
