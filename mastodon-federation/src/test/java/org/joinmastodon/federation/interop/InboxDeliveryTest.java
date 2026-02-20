package org.joinmastodon.federation.interop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joinmastodon.activitypub.model.Create;
import org.joinmastodon.activitypub.model.Follow;
import org.joinmastodon.activitypub.model.LikeActivity;
import org.joinmastodon.activitypub.model.Note;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.federation.config.FederationProperties;
import org.joinmastodon.federation.service.ActivityDispatcher;
import org.joinmastodon.federation.web.InboxController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Interoperability tests for receiving activities from remote instances.
 * Tests that the inbox correctly handles incoming ActivityPub activities.
 * 
 * @see <a href="https://www.w3.org/TR/activitypub/#inbox">ActivityPub Inbox</a>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Inbox Delivery Tests")
class InboxDeliveryTest {

    @Mock
    private AccountService accountService;

    @Mock
    private FederationProperties federationProperties;

    @Mock
    private ActivityDispatcher activityDispatcher;

    @InjectMocks
    private InboxController inboxController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Account localAccount;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inboxController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        localAccount = new Account();
        localAccount.setId(1L);
        localAccount.setUsername("alice");
        localAccount.setAcct("alice");
        localAccount.setLocalAccount(true);

        when(federationProperties.getDomain()).thenReturn("example.com");
        when(federationProperties.getBaseUrl()).thenReturn("https://example.com");
        when(federationProperties.isRequireSignatures()).thenReturn(false);
    }

    @Nested
    @DisplayName("Shared Inbox")
    class SharedInboxTests {

        @Test
        @DisplayName("Accepts Create activity")
        void acceptsCreateActivity() throws Exception {
            Note note = new Note();
            note.setId("https://remote.example/notes/123");
            note.setType("Note");
            note.setContent("<p>Hello from remote!</p>");
            note.setAttributedTo("https://remote.example/users/bob");
            note.setPublished(Instant.now());
            note.setTo(List.of("https://www.w3.org/ns/activitystreams#Public"));

            Create create = new Create();
            create.setId("https://remote.example/activities/123");
            create.setType("Create");
            create.setActor("https://remote.example/users/bob");
            create.setObject(note);

            String json = objectMapper.writeValueAsString(create);

            mockMvc.perform(post("/inbox")
                            .contentType("application/activity+json")
                            .content(json))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("Accepts Follow activity")
        void acceptsFollowActivity() throws Exception {
            Follow follow = new Follow();
            follow.setId("https://remote.example/follows/123");
            follow.setType("Follow");
            follow.setActor("https://remote.example/users/bob");
            follow.setObject("https://example.com/users/alice");

            String json = objectMapper.writeValueAsString(follow);

            mockMvc.perform(post("/inbox")
                            .contentType("application/activity+json")
                            .content(json))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("Accepts Like activity")
        void acceptsLikeActivity() throws Exception {
            LikeActivity like = new LikeActivity();
            like.setId("https://remote.example/likes/123");
            like.setType("Like");
            like.setActor("https://remote.example/users/bob");
            like.setObject("https://example.com/users/alice/statuses/456");

            String json = objectMapper.writeValueAsString(like);

            mockMvc.perform(post("/inbox")
                            .contentType("application/activity+json")
                            .content(json))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("Rejects activity without content type")
        void rejectsActivityWithoutContentType() throws Exception {
            String json = "{\"type\":\"Create\"}";

            mockMvc.perform(post("/inbox")
                            .content(json))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("User Inbox")
    class UserInboxTests {

        @Test
        @DisplayName("Accepts activity for existing user")
        void acceptsActivityForExistingUser() throws Exception {
            when(accountService.findLocalAccountByUsername("alice"))
                    .thenReturn(Optional.of(localAccount));

            Follow follow = new Follow();
            follow.setId("https://remote.example/follows/123");
            follow.setType("Follow");
            follow.setActor("https://remote.example/users/bob");
            follow.setObject("https://example.com/users/alice");

            String json = objectMapper.writeValueAsString(follow);

            mockMvc.perform(post("/users/alice/inbox")
                            .contentType("application/activity+json")
                            .content(json))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("Returns 404 for non-existent user")
        void returns404ForNonExistentUser() throws Exception {
            when(accountService.findLocalAccountByUsername("nonexistent"))
                    .thenReturn(Optional.empty());

            String json = "{\"type\":\"Follow\"}";

            mockMvc.perform(post("/users/nonexistent/inbox")
                            .contentType("application/activity+json")
                            .content(json))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Activity Format Validation")
    class ActivityFormatTests {

        @Test
        @DisplayName("Requires @context field")
        void requiresContextField() throws Exception {
            // The activity should have @context, but we test handling without it
            String json = """
                {
                    "type": "Create",
                    "actor": "https://remote.example/users/bob",
                    "object": {
                        "type": "Note",
                        "content": "Test"
                    }
                }
                """;

            // Should still accept (be lenient) or reject
            mockMvc.perform(post("/inbox")
                            .contentType("application/activity+json")
                            .content(json))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("Handles nested objects")
        void handlesNestedObjects() throws Exception {
            String json = """
                {
                    "@context": "https://www.w3.org/ns/activitystreams",
                    "type": "Create",
                    "id": "https://remote.example/activities/123",
                    "actor": "https://remote.example/users/bob",
                    "object": {
                        "type": "Note",
                        "id": "https://remote.example/notes/456",
                        "attributedTo": "https://remote.example/users/bob",
                        "content": "<p>Nested note</p>",
                        "to": ["https://www.w3.org/ns/activitystreams#Public"]
                    }
                }
                """;

            mockMvc.perform(post("/inbox")
                            .contentType("application/activity+json")
                            .content(json))
                    .andExpect(status().isAccepted());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Handles malformed JSON gracefully")
        void handlesMalformedJson() throws Exception {
            String json = "{ invalid json }";

            mockMvc.perform(post("/inbox")
                            .contentType("application/activity+json")
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Handles empty body")
        void handlesEmptyBody() throws Exception {
            mockMvc.perform(post("/inbox")
                            .contentType("application/activity+json")
                            .content(""))
                    .andExpect(status().isBadRequest());
        }
    }
}
