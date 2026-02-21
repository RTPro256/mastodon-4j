package org.joinmastodon.core.service;

import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Mention;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.joinmastodon.core.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StatusVisibilityService.
 * Tests the visibility filtering logic for statuses.
 */
@ExtendWith(MockitoExtension.class)
class StatusVisibilityServiceTest {

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private StatusVisibilityService statusVisibilityService;

    private Account author;
    private Account viewer;
    private Account follower;
    private Account stranger;
    private Status status;

    @BeforeEach
    void setUp() {
        author = createAccount(1L, "author", "author@example.test");
        viewer = createAccount(2L, "viewer", "viewer@example.test");
        follower = createAccount(3L, "follower", "follower@example.test");
        stranger = createAccount(4L, "stranger", "stranger@example.test");
        
        status = new Status();
        status.setAccount(author);
        status.setContent("Test content");
    }

    @Nested
    @DisplayName("Null status handling")
    class NullStatusTests {

        @Test
        @DisplayName("Returns false for null status")
        void nullStatusReturnsFalse() {
            boolean result = statusVisibilityService.canView(null, viewer);
            
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Returns true for public status with null viewer")
        void publicStatusWithNullViewerReturnsTrue() {
            status.setVisibility(Visibility.PUBLIC);
            
            boolean result = statusVisibilityService.canView(status, null);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Returns true for unlisted status with null viewer")
        void unlistedStatusWithNullViewerReturnsTrue() {
            status.setVisibility(Visibility.UNLISTED);
            
            boolean result = statusVisibilityService.canView(status, null);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Returns false for private status with null viewer")
        void privateStatusWithNullViewerReturnsFalse() {
            status.setVisibility(Visibility.PRIVATE);
            
            boolean result = statusVisibilityService.canView(status, null);
            
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Returns false for direct status with null viewer")
        void directStatusWithNullViewerReturnsFalse() {
            status.setVisibility(Visibility.DIRECT);
            
            boolean result = statusVisibilityService.canView(status, null);
            
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Public visibility")
    class PublicVisibilityTests {

        @Test
        @DisplayName("Public status is visible to anyone")
        void publicStatusVisibleToAnyone() {
            status.setVisibility(Visibility.PUBLIC);
            
            assertThat(statusVisibilityService.canView(status, viewer)).isTrue();
            assertThat(statusVisibilityService.canView(status, stranger)).isTrue();
            assertThat(statusVisibilityService.canView(status, null)).isTrue();
        }

        @Test
        @DisplayName("Null visibility defaults to public")
        void nullVisibilityDefaultsToPublic() {
            status.setVisibility(null);
            
            assertThat(statusVisibilityService.canView(status, viewer)).isTrue();
            assertThat(statusVisibilityService.canView(status, null)).isTrue();
        }
    }

    @Nested
    @DisplayName("Unlisted visibility")
    class UnlistedVisibilityTests {

        @Test
        @DisplayName("Unlisted status is visible to anyone")
        void unlistedStatusVisibleToAnyone() {
            status.setVisibility(Visibility.UNLISTED);
            
            assertThat(statusVisibilityService.canView(status, viewer)).isTrue();
            assertThat(statusVisibilityService.canView(status, stranger)).isTrue();
            assertThat(statusVisibilityService.canView(status, null)).isTrue();
        }
    }

    @Nested
    @DisplayName("Private visibility")
    class PrivateVisibilityTests {

        @BeforeEach
        void setUpPrivate() {
            status.setVisibility(Visibility.PRIVATE);
        }

        @Test
        @DisplayName("Author can view their own private status")
        void authorCanViewOwnPrivateStatus() {
            boolean result = statusVisibilityService.canView(status, author);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Follower can view private status")
        void followerCanViewPrivateStatus() {
            when(followRepository.findByAccountAndTargetAccount(follower, author))
                    .thenReturn(Optional.of(createFollow(follower, author)));
            
            boolean result = statusVisibilityService.canView(status, follower);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Non-follower cannot view private status")
        void nonFollowerCannotViewPrivateStatus() {
            when(followRepository.findByAccountAndTargetAccount(stranger, author))
                    .thenReturn(Optional.empty());
            
            boolean result = statusVisibilityService.canView(status, stranger);
            
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Null viewer cannot view private status")
        void nullViewerCannotViewPrivateStatus() {
            boolean result = statusVisibilityService.canView(status, null);
            
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Direct visibility")
    class DirectVisibilityTests {

        @BeforeEach
        void setUpDirect() {
            status.setVisibility(Visibility.DIRECT);
        }

        @Test
        @DisplayName("Author can view their own direct status")
        void authorCanViewOwnDirectStatus() {
            boolean result = statusVisibilityService.canView(status, author);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Mentioned user can view direct status")
        void mentionedUserCanViewDirectStatus() {
            Mention mention = new Mention();
            mention.setAccount(viewer);
            status.setMentions(List.of(mention));
            
            boolean result = statusVisibilityService.canView(status, viewer);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Non-mentioned user cannot view direct status")
        void nonMentionedUserCannotViewDirectStatus() {
            status.setMentions(new ArrayList<>());
            
            boolean result = statusVisibilityService.canView(status, stranger);
            
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Direct status with null mentions is not visible to others")
        void directStatusWithNullMentionsNotVisible() {
            status.setMentions(null);
            
            boolean result = statusVisibilityService.canView(status, stranger);
            
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Direct status with empty mentions is not visible to others")
        void directStatusWithEmptyMentionsNotVisible() {
            status.setMentions(new ArrayList<>());
            
            boolean result = statusVisibilityService.canView(status, stranger);
            
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Multiple mentioned users can view direct status")
        void multipleMentionedUsersCanViewDirectStatus() {
            Mention mention1 = new Mention();
            mention1.setAccount(viewer);
            Mention mention2 = new Mention();
            mention2.setAccount(follower);
            status.setMentions(List.of(mention1, mention2));
            
            assertThat(statusVisibilityService.canView(status, viewer)).isTrue();
            assertThat(statusVisibilityService.canView(status, follower)).isTrue();
            assertThat(statusVisibilityService.canView(status, stranger)).isFalse();
        }

        @Test
        @DisplayName("Mention with null account is handled gracefully")
        void mentionWithNullAccountHandledGracefully() {
            Mention mention = new Mention();
            mention.setAccount(null);
            status.setMentions(List.of(mention));
            
            boolean result = statusVisibilityService.canView(status, viewer);
            
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Author's own statuses")
    class AuthorOwnStatusTests {

        @Test
        @DisplayName("Author can view their own public status")
        void authorCanViewOwnPublicStatus() {
            status.setVisibility(Visibility.PUBLIC);
            
            boolean result = statusVisibilityService.canView(status, author);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Author can view their own private status")
        void authorCanViewOwnPrivateStatus() {
            status.setVisibility(Visibility.PRIVATE);
            
            boolean result = statusVisibilityService.canView(status, author);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Author can view their own direct status")
        void authorCanViewOwnDirectStatus() {
            status.setVisibility(Visibility.DIRECT);
            
            boolean result = statusVisibilityService.canView(status, author);
            
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Status with null account")
    class StatusWithNullAccountTests {

        @Test
        @DisplayName("Public status with null account is visible")
        void publicStatusWithNullAccountIsVisible() {
            status.setVisibility(Visibility.PUBLIC);
            status.setAccount(null);
            
            boolean result = statusVisibilityService.canView(status, viewer);
            
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Private status with null account is not visible")
        void privateStatusWithNullAccountNotVisible() {
            status.setVisibility(Visibility.PRIVATE);
            status.setAccount(null);
            
            when(followRepository.findByAccountAndTargetAccount(any(), any()))
                    .thenReturn(Optional.empty());
            
            boolean result = statusVisibilityService.canView(status, viewer);
            
            assertThat(result).isFalse();
        }
    }

    // Helper methods

    private Account createAccount(Long id, String username, String domain) {
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        account.setDomain(domain);
        account.setAcct(username + "@" + domain);
        account.setDisplayName(username);
        return account;
    }

    private org.joinmastodon.core.entity.Follow createFollow(Account follower, Account following) {
        org.joinmastodon.core.entity.Follow follow = new org.joinmastodon.core.entity.Follow();
        follow.setAccount(follower);
        follow.setTargetAccount(following);
        return follow;
    }
}
