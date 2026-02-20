package org.joinmastodon.federation.interop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.federation.config.FederationProperties;
import org.joinmastodon.federation.web.WebFingerController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Interoperability tests for WebFinger discovery.
 * Tests compatibility with RFC 7033 and Mastodon's WebFinger implementation.
 * 
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7033">RFC 7033 - WebFinger</a>
 * @see <a href="https://docs.joinmastodon.org/spec/webfinger/">Mastodon WebFinger</a>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebFinger Interoperability Tests")
class WebFingerInteropTest {

    @Mock
    private AccountService accountService;

    @Mock
    private FederationProperties federationProperties;

    @InjectMocks
    private WebFingerController webFingerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webFingerController).build();
        objectMapper = new ObjectMapper();

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUsername("alice");
        testAccount.setAcct("alice");
        testAccount.setDisplayName("Alice");
        testAccount.setLocalAccount(true);

        when(federationProperties.getDomain()).thenReturn("example.com");
        when(federationProperties.getBaseUrl()).thenReturn("https://example.com");
    }

    @Nested
    @DisplayName("RFC 7033 Compliance")
    class Rfc7033Tests {

        @Test
        @DisplayName("Returns JSON response for valid resource")
        void returnsJsonForValidResource() throws Exception {
            when(accountService.findByUsernameAndDomain("alice", null))
                    .thenReturn(Optional.of(testAccount));

            mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:alice@example.com")
                            .accept("application/jrd+json"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subject").value("acct:alice@example.com"));
        }

        @Test
        @DisplayName("Returns 404 for non-existent resource")
        void returns404ForNonExistentResource() throws Exception {
            when(accountService.findByUsernameAndDomain("nonexistent", null))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:nonexistent@example.com"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Returns 400 for missing resource parameter")
        void returns400ForMissingResource() throws Exception {
            mockMvc.perform(get("/.well-known/webfinger"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 404 for wrong domain")
        void returns404ForWrongDomain() throws Exception {
            mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:alice@otherdomain.com"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("JRD Document Format")
    class JrdFormatTests {

        @Test
        @DisplayName("Includes subject field")
        void includesSubjectField() throws Exception {
            when(accountService.findByUsernameAndDomain("alice", null))
                    .thenReturn(Optional.of(testAccount));

            MvcResult result = mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:alice@example.com"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
            assertThat(json.has("subject")).isTrue();
            assertThat(json.get("subject").asText()).isEqualTo("acct:alice@example.com");
        }

        @Test
        @DisplayName("Includes aliases field")
        void includesAliasesField() throws Exception {
            when(accountService.findByUsernameAndDomain("alice", null))
                    .thenReturn(Optional.of(testAccount));

            MvcResult result = mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:alice@example.com"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
            assertThat(json.has("aliases")).isTrue();
            assertThat(json.get("aliases").isArray()).isTrue();
        }

        @Test
        @DisplayName("Includes links field")
        void includesLinksField() throws Exception {
            when(accountService.findByUsernameAndDomain("alice", null))
                    .thenReturn(Optional.of(testAccount));

            MvcResult result = mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:alice@example.com"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
            assertThat(json.has("links")).isTrue();
            assertThat(json.get("links").isArray()).isTrue();
        }

        @Test
        @DisplayName("Includes self link with ActivityPub profile")
        void includesSelfLinkWithActivityPubProfile() throws Exception {
            when(accountService.findByUsernameAndDomain("alice", null))
                    .thenReturn(Optional.of(testAccount));

            MvcResult result = mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:alice@example.com"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode links = json.get("links");
            
            boolean foundSelfLink = false;
            for (JsonNode link : links) {
                if ("self".equals(link.get("rel").asText())) {
                    foundSelfLink = true;
                    assertThat(link.get("type").asText()).isEqualTo("application/activity+json");
                    assertThat(link.get("href").asText()).isEqualTo("https://example.com/users/alice");
                    break;
                }
            }
            assertThat(foundSelfLink).isTrue();
        }

        @Test
        @DisplayName("Includes profile link")
        void includesProfileLink() throws Exception {
            when(accountService.findByUsernameAndDomain("alice", null))
                    .thenReturn(Optional.of(testAccount));

            MvcResult result = mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:alice@example.com"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode links = json.get("links");
            
            boolean foundProfileLink = false;
            for (JsonNode link : links) {
                if ("http://webfinger.net/rel/profile-page".equals(link.get("rel").asText())) {
                    foundProfileLink = true;
                    assertThat(link.get("type").asText()).isEqualTo("text/html");
                    assertThat(link.get("href").asText()).contains("alice");
                    break;
                }
            }
            assertThat(foundProfileLink).isTrue();
        }
    }

    @Nested
    @DisplayName("Mastodon Compatibility")
    class MastodonCompatibilityTests {

        @Test
        @DisplayName("Accepts resource parameter with acct: scheme")
        void acceptsAcctScheme() throws Exception {
            when(accountService.findByUsernameAndDomain("alice", null))
                    .thenReturn(Optional.of(testAccount));

            mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:alice@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subject").value("acct:alice@example.com"));
        }

        @Test
        @DisplayName("Handles case-insensitive username lookup")
        void handlesCaseInsensitiveUsername() throws Exception {
            when(accountService.findByUsernameAndDomain(anyString(), any()))
                    .thenReturn(Optional.of(testAccount));

            mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:ALICE@example.com"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Returns correct content type")
        void returnsCorrectContentType() throws Exception {
            when(accountService.findByUsernameAndDomain("alice", null))
                    .thenReturn(Optional.of(testAccount));

            mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:alice@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(result -> {
                        String contentType = result.getResponse().getContentType();
                        assertThat(contentType).contains("application/json");
                    });
        }
    }

    @Nested
    @DisplayName("Remote User Discovery")
    class RemoteUserDiscoveryTests {

        @Test
        @DisplayName("Returns 404 for remote users")
        void returns404ForRemoteUsers() throws Exception {
            // WebFinger should only return local users
            Account remoteAccount = new Account();
            remoteAccount.setId(2L);
            remoteAccount.setUsername("bob");
            remoteAccount.setAcct("bob@remote.example");
            remoteAccount.setLocalAccount(false);

            when(accountService.findByUsernameAndDomain("bob", "remote.example"))
                    .thenReturn(Optional.of(remoteAccount));

            mockMvc.perform(get("/.well-known/webfinger")
                            .param("resource", "acct:bob@remote.example"))
                    .andExpect(status().isNotFound());
        }
    }
}
