package org.joinmastodon.federation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joinmastodon.activitypub.model.Actor;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.federation.config.FederationProperties;
import org.joinmastodon.federation.service.ActivityPubMapper;
import org.joinmastodon.federation.service.RemoteActorService;
import org.joinmastodon.federation.web.ActorController;
import org.joinmastodon.federation.web.InboxController;
import org.joinmastodon.federation.web.OutboxController;
import org.joinmastodon.federation.web.WebFingerController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for federation controllers using MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class FederationIntegrationTest {

    @Mock
    private AccountService accountService;

    @Mock
    private ActivityPubMapper activityPubMapper;

    @Mock
    private RemoteActorService remoteActorService;

    @Mock
    private FederationProperties federationProperties;

    @InjectMocks
    private ActorController actorController;

    @InjectMocks
    private InboxController inboxController;

    @InjectMocks
    private OutboxController outboxController;

    @InjectMocks
    private WebFingerController webFingerController;

    private MockMvc mockMvc;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Setup MockMvc for all controllers
        mockMvc = MockMvcBuilders.standaloneSetup(actorController, inboxController, outboxController, webFingerController)
                .build();

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUsername("testuser");
        testAccount.setAcct("testuser");
        testAccount.setDisplayName("Test User");
        testAccount.setLocalAccount(true);
        testAccount.setFollowersCount(10);
        testAccount.setFollowingCount(5);

        when(federationProperties.getBaseUrl()).thenReturn("https://example.com");
        when(federationProperties.getDomain()).thenReturn("example.com");
    }

    @Test
    void getActor_shouldReturnActorProfile() throws Exception {
        when(accountService.findLocalAccountByUsername("testuser"))
                .thenReturn(Optional.of(testAccount));

        Actor actor = new Actor();
        actor.setId("https://example.com/users/testuser");
        actor.setPreferredUsername("testuser");
        when(activityPubMapper.toActor(any(Account.class))).thenReturn(actor);

        mockMvc.perform(get("/users/testuser")
                        .accept("application/activity+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("https://example.com/users/testuser"))
                .andExpect(jsonPath("$.preferredUsername").value("testuser"));
    }

    @Test
    void getActor_shouldReturn404ForNonExistentUser() throws Exception {
        when(accountService.findLocalAccountByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/users/nonexistent")
                        .accept("application/activity+json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFollowers_shouldReturnCollection() throws Exception {
        when(accountService.findLocalAccountByUsername("testuser"))
                .thenReturn(Optional.of(testAccount));

        mockMvc.perform(get("/users/testuser/followers")
                        .accept("application/activity+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("OrderedCollection"))
                .andExpect(jsonPath("$.totalItems").value(10));
    }

    @Test
    void getFollowing_shouldReturnCollection() throws Exception {
        when(accountService.findLocalAccountByUsername("testuser"))
                .thenReturn(Optional.of(testAccount));

        mockMvc.perform(get("/users/testuser/following")
                        .accept("application/activity+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("OrderedCollection"))
                .andExpect(jsonPath("$.totalItems").value(5));
    }

    @Test
    void getOutbox_shouldReturnCollection() throws Exception {
        when(accountService.findLocalAccountByUsername("testuser"))
                .thenReturn(Optional.of(testAccount));

        mockMvc.perform(get("/users/testuser/outbox")
                        .accept("application/activity+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("OrderedCollection"));
    }

    @Test
    void sharedInbox_shouldReturn202() throws Exception {
        String activity = """
                {
                    "@context": "https://www.w3.org/ns/activitystreams",
                    "type": "Create",
                    "id": "https://remote.example/activities/123",
                    "actor": "https://remote.example/users/remoteuser",
                    "object": {
                        "type": "Note",
                        "id": "https://remote.example/notes/456",
                        "content": "Hello world"
                    }
                }
                """;

        when(federationProperties.isRequireSignatures()).thenReturn(false);

        mockMvc.perform(post("/inbox")
                        .contentType("application/activity+json")
                        .content(activity))
                .andExpect(status().isAccepted());
    }

    @Test
    void userInbox_shouldReturn404ForNonExistentUser() throws Exception {
        when(accountService.findLocalAccountByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        String activity = """
                {
                    "@context": "https://www.w3.org/ns/activitystreams",
                    "type": "Follow",
                    "actor": "https://remote.example/users/remoteuser",
                    "object": "https://example.com/users/nonexistent"
                }
                """;

        mockMvc.perform(post("/users/nonexistent/inbox")
                        .contentType("application/activity+json")
                        .content(activity))
                .andExpect(status().isNotFound());
    }

    @Test
    void webfinger_shouldReturnResponseForLocalUser() throws Exception {
        when(accountService.findByUsernameAndDomain("testuser", null))
                .thenReturn(Optional.of(testAccount));

        mockMvc.perform(get("/.well-known/webfinger")
                        .param("resource", "acct:testuser@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("acct:testuser@example.com"));
    }

    @Test
    void webfinger_shouldReturn404ForNonExistentUser() throws Exception {
        when(accountService.findByUsernameAndDomain("nonexistent", null))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/.well-known/webfinger")
                        .param("resource", "acct:nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void webfinger_shouldReturn404ForWrongDomain() throws Exception {
        mockMvc.perform(get("/.well-known/webfinger")
                        .param("resource", "acct:testuser@otherdomain.com"))
                .andExpect(status().isNotFound());
    }
}
