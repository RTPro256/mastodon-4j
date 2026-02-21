package org.joinmastodon.contentaccess.service;

import org.joinmastodon.contentaccess.entity.ContentAccess;
import org.joinmastodon.contentaccess.entity.ContentAccess.AccessLevel;
import org.joinmastodon.contentaccess.entity.ContentPermission;
import org.joinmastodon.contentaccess.entity.ContentPermission.Permission;
import org.joinmastodon.contentaccess.repository.ContentAccessRepository;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.repository.AccountRepository;
import org.joinmastodon.core.repository.StatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ContentAccessService.
 * Tests content access control and permission management.
 */
@ExtendWith(MockitoExtension.class)
class ContentAccessServiceTest {

    @Mock
    private ContentAccessRepository contentAccessRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ContentAccessService contentAccessService;

    private Status testStatus;
    private Account testAccount;
    private Account granteeAccount;
    private ContentAccess contentAccess;

    @BeforeEach
    void setUp() {
        testStatus = createStatus(1L, "Test content");
        testAccount = createAccount(1L, "alice", "example.test");
        granteeAccount = createAccount(2L, "bob", "example.test");
        
        contentAccess = new ContentAccess(testStatus, AccessLevel.PUBLIC);
        contentAccess.setId(1L);
    }

    @Nested
    @DisplayName("Create content access")
    class CreateContentAccessTests {

        @Test
        @DisplayName("Creates content access for existing status")
        void createsContentAccessForExistingStatus() {
            when(statusRepository.findById(1L)).thenReturn(Optional.of(testStatus));
            when(contentAccessRepository.save(any(ContentAccess.class))).thenAnswer(inv -> {
                ContentAccess ca = inv.getArgument(0);
                ca.setId(1L);
                return ca;
            });
            
            ContentAccess result = contentAccessService.createContentAccess(1L, AccessLevel.PRIVATE);
            
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(testStatus);
            assertThat(result.getAccessLevel()).isEqualTo(AccessLevel.PRIVATE);
        }

        @Test
        @DisplayName("Throws exception for non-existent status")
        void throwsForNonExistentStatus() {
            when(statusRepository.findById(999L)).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> contentAccessService.createContentAccess(999L, AccessLevel.PUBLIC))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Status not found");
        }
    }

    @Nested
    @DisplayName("Get content access")
    class GetContentAccessTests {

        @Test
        @DisplayName("Returns content access for status")
        void returnsContentAccessForStatus() {
            when(contentAccessRepository.findByStatusId(1L)).thenReturn(Optional.of(contentAccess));
            
            Optional<ContentAccess> result = contentAccessService.getContentAccess(1L);
            
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(contentAccess);
        }

        @Test
        @DisplayName("Returns empty for status without content access")
        void returnsEmptyForStatusWithoutContentAccess() {
            when(contentAccessRepository.findByStatusId(999L)).thenReturn(Optional.empty());
            
            Optional<ContentAccess> result = contentAccessService.getContentAccess(999L);
            
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update access level")
    class UpdateAccessLevelTests {

        @Test
        @DisplayName("Updates access level for existing content")
        void updatesAccessLevelForExistingContent() {
            when(contentAccessRepository.findByStatusId(1L)).thenReturn(Optional.of(contentAccess));
            when(contentAccessRepository.save(any(ContentAccess.class))).thenReturn(contentAccess);
            
            ContentAccess result = contentAccessService.updateAccessLevel(1L, AccessLevel.PRIVATE);
            
            assertThat(result.getAccessLevel()).isEqualTo(AccessLevel.PRIVATE);
            verify(contentAccessRepository).save(contentAccess);
        }

        @Test
        @DisplayName("Throws exception for non-existent content access")
        void throwsForNonExistentContentAccess() {
            when(contentAccessRepository.findByStatusId(999L)).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> contentAccessService.updateAccessLevel(999L, AccessLevel.PRIVATE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Content access not found");
        }
    }

    @Nested
    @DisplayName("Grant permission")
    class GrantPermissionTests {

        @Test
        @DisplayName("Grants permission to account")
        void grantsPermissionToAccount() {
            when(contentAccessRepository.findByStatusId(1L)).thenReturn(Optional.of(contentAccess));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(granteeAccount));
            when(contentAccessRepository.save(any(ContentAccess.class))).thenReturn(contentAccess);
            
            ContentPermission result = contentAccessService.grantPermission(
                    1L, 2L, Permission.VIEW, 1L);
            
            assertThat(result).isNotNull();
            assertThat(result.getPermission()).isEqualTo(Permission.VIEW);
            assertThat(result.getGrantee()).isEqualTo(granteeAccount);
        }

        @Test
        @DisplayName("Throws exception for non-existent content access")
        void throwsForNonExistentContentAccess() {
            when(contentAccessRepository.findByStatusId(999L)).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> contentAccessService.grantPermission(
                    999L, 2L, Permission.VIEW, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Content access not found");
        }

        @Test
        @DisplayName("Throws exception for non-existent grantee account")
        void throwsForNonExistentGranteeAccount() {
            when(contentAccessRepository.findByStatusId(1L)).thenReturn(Optional.of(contentAccess));
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());
            
            assertThatThrownBy(() -> contentAccessService.grantPermission(
                    1L, 999L, Permission.VIEW, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account not found");
        }
    }

    @Nested
    @DisplayName("Content access entity tests")
    class ContentAccessEntityTests {

        @Test
        @DisplayName("New content access has default public access level")
        void newContentAccessHasDefaultPublicAccessLevel() {
            ContentAccess ca = new ContentAccess();
            
            assertThat(ca.getAccessLevel()).isEqualTo(AccessLevel.PUBLIC);
        }

        @Test
        @DisplayName("Content access is not expired by default")
        void contentAccessNotExpiredByDefault() {
            ContentAccess ca = new ContentAccess(testStatus, AccessLevel.PUBLIC);
            
            assertThat(ca.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Content access with past expiry is expired")
        void contentAccessWithPastExpiryIsExpired() {
            ContentAccess ca = new ContentAccess(testStatus, AccessLevel.PUBLIC);
            ca.setExpiresAt(java.time.Instant.now().minusSeconds(3600));
            
            assertThat(ca.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Content access with future expiry is not expired")
        void contentAccessWithFutureExpiryIsNotExpired() {
            ContentAccess ca = new ContentAccess(testStatus, AccessLevel.PUBLIC);
            ca.setExpiresAt(java.time.Instant.now().plusSeconds(3600));
            
            assertThat(ca.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Add permission establishes bidirectional relationship")
        void addPermissionEstablishesBidirectionalRelationship() {
            ContentAccess ca = new ContentAccess(testStatus, AccessLevel.PRIVATE);
            ContentPermission permission = new ContentPermission();
            
            ca.addPermission(permission);
            
            assertThat(ca.getPermissions()).contains(permission);
            assertThat(permission.getContentAccess()).isEqualTo(ca);
        }

        @Test
        @DisplayName("Remove permission breaks bidirectional relationship")
        void removePermissionBreaksBidirectionalRelationship() {
            ContentAccess ca = new ContentAccess(testStatus, AccessLevel.PRIVATE);
            ContentPermission permission = new ContentPermission();
            ca.addPermission(permission);
            
            ca.removePermission(permission);
            
            assertThat(ca.getPermissions()).doesNotContain(permission);
            assertThat(permission.getContentAccess()).isNull();
        }
    }

    // Helper methods

    private Status createStatus(Long id, String content) {
        Status status = new Status();
        status.setId(id);
        status.setContent(content);
        return status;
    }

    private Account createAccount(Long id, String username, String domain) {
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        account.setDomain(domain);
        account.setAcct(username + "@" + domain);
        return account;
    }
}
