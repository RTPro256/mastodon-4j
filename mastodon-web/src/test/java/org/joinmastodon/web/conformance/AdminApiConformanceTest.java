package org.joinmastodon.web.conformance;

import com.fasterxml.jackson.databind.JsonNode;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.Report;
import org.joinmastodon.core.entity.User;
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
 * API Conformance tests for Admin endpoints.
 * Tests compatibility with Mastodon API v4.5.6.
 * 
 * @see <a href="https://docs.joinmastodon.org/methods/admin/">Mastodon Admin API</a>
 */
@DisplayName("Admin API Conformance Tests")
class AdminApiConformanceTest extends BaseApiConformanceTest {

    private TestUser adminUser;
    private TestUser regularUser;
    private Application adminApp;
    private String adminToken;
    private String regularToken;

    @BeforeEach
    void setupAdmin() {
        clearTables();
        new TransactionTemplate(transactionManager).executeWithoutResult(txStatus -> {
            adminUser = createTestAccount("admin", "admin@example.test");
            regularUser = createTestAccount("user", "user@example.test");
            
            // Make admin user an admin
            adminUser.user().setRole(User.Role.ADMIN);
            entityManager.merge(adminUser.user());
            
            adminApp = createTestApplication("Admin App", "read write admin:read admin:write");
            adminToken = createAccessToken(adminUser.user(), adminApp, "read write admin:read admin:write");
            regularToken = createAccessToken(regularUser.user(), adminApp, "read write");
            
            entityManager.flush();
            entityManager.clear();
        });
    }

    @Nested
    @DisplayName("GET /api/v1/admin/accounts")
    class AdminAccountsTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void adminAccountsReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/admin/accounts"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns 403 for non-admin users")
        void adminAccountsReturns403ForNonAdmin() throws Exception {
            try {
                getJsonWithAuth("/api/v1/admin/accounts", regularToken);
                throw new AssertionError("Expected 403 Forbidden");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }
        }

        @Test
        @DisplayName("Returns accounts for admin users")
        void adminAccountsReturnsAccountsForAdmin() throws Exception {
            JsonNode accounts = getJsonWithAuth("/api/v1/admin/accounts", adminToken);
            
            assertThat(accounts.isArray()).isTrue();
            assertThat(accounts.size()).isGreaterThanOrEqualTo(1);
            
            for (JsonNode account : accounts) {
                // Admin account format differs from regular account format
                assertThat(account.has("id")).isTrue();
                assertThat(account.has("username")).isTrue();
                assertThat(account.has("domain")).isTrue();
                assertThat(account.has("email")).isTrue();
            }
        }

        @Test
        @DisplayName("Respects limit parameter")
        void adminAccountsRespectsLimit() throws Exception {
            JsonNode accounts = getJsonWithAuth("/api/v1/admin/accounts?limit=1", adminToken);
            
            assertThat(accounts.size()).isLessThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/accounts/:id")
    class AdminAccountDetailTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void adminAccountDetailReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(
                        baseUrl("/api/v1/admin/accounts/" + regularUser.account().getId()),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns 403 for non-admin users")
        void adminAccountDetailReturns403ForNonAdmin() throws Exception {
            try {
                getJsonWithAuth(
                        "/api/v1/admin/accounts/" + regularUser.account().getId(),
                        regularToken);
                throw new AssertionError("Expected 403 Forbidden");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }
        }

        @Test
        @DisplayName("Returns account details for admin users")
        void adminAccountDetailReturnsAccountForAdmin() throws Exception {
            JsonNode account = getJsonWithAuth(
                    "/api/v1/admin/accounts/" + regularUser.account().getId(),
                    adminToken);
            
            assertThat(account.has("id")).isTrue();
            assertThat(account.has("username")).isTrue();
            assertThat(account.has("email")).isTrue();
            assertThat(account.has("account")).isTrue();
        }

        @Test
        @DisplayName("Returns 404 for non-existent account")
        void adminAccountDetailReturns404ForNonExistent() throws Exception {
            try {
                getJsonWithAuth("/api/v1/admin/accounts/999999999", adminToken);
                throw new AssertionError("Expected 404 Not Found");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/reports")
    class AdminReportsTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void adminReportsReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/admin/reports"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns 403 for non-admin users")
        void adminReportsReturns403ForNonAdmin() throws Exception {
            try {
                getJsonWithAuth("/api/v1/admin/reports", regularToken);
                throw new AssertionError("Expected 403 Forbidden");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }
        }

        @Test
        @DisplayName("Returns reports for admin users")
        void adminReportsReturnsReportsForAdmin() throws Exception {
            JsonNode reports = getJsonWithAuth("/api/v1/admin/reports", adminToken);
            
            assertThat(reports.isArray()).isTrue();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/accounts/:id/action")
    class AdminAccountActionTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void adminAccountActionReturns401WhenUnauthenticated() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String payload = """
                {
                  "type": "disable",
                  "text": "Account suspended"
                }
                """;
            
            try {
                restTemplate.postForEntity(
                        baseUrl("/api/v1/admin/accounts/" + regularUser.account().getId() + "/action"),
                        new HttpEntity<>(payload, headers),
                        String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns 403 for non-admin users")
        void adminAccountActionReturns403ForNonAdmin() throws Exception {
            String payload = """
                {
                  "type": "disable",
                  "text": "Account suspended"
                }
                """;
            
            try {
                postJsonWithAuth(
                        "/api/v1/admin/accounts/" + regularUser.account().getId() + "/action",
                        payload,
                        regularToken);
                throw new AssertionError("Expected 403 Forbidden");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/domain_blocks")
    class AdminDomainBlocksTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void adminDomainBlocksReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/admin/domain_blocks"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns 403 for non-admin users")
        void adminDomainBlocksReturns403ForNonAdmin() throws Exception {
            try {
                getJsonWithAuth("/api/v1/admin/domain_blocks", regularToken);
                throw new AssertionError("Expected 403 Forbidden");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }
        }

        @Test
        @DisplayName("Returns domain blocks for admin users")
        void adminDomainBlocksReturnsBlocksForAdmin() throws Exception {
            JsonNode blocks = getJsonWithAuth("/api/v1/admin/domain_blocks", adminToken);
            
            assertThat(blocks.isArray()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/instance")
    class AdminInstanceTests {

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void adminInstanceReturns401WhenUnauthenticated() throws Exception {
            try {
                restTemplate.getForEntity(baseUrl("/api/v1/admin/instance"), String.class);
                throw new AssertionError("Expected 401 Unauthorized");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            }
        }

        @Test
        @DisplayName("Returns 403 for non-admin users")
        void adminInstanceReturns403ForNonAdmin() throws Exception {
            try {
                getJsonWithAuth("/api/v1/admin/instance", regularToken);
                throw new AssertionError("Expected 403 Forbidden");
            } catch (HttpClientErrorException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            }
        }
    }
}
