package org.joinmastodon.core.service;

import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Follow;
import org.joinmastodon.core.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FollowService.
 * Tests follow/unfollow operations and relationship management.
 */
@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private FollowService followService;

    private Account alice;
    private Account bob;
    private Account charlie;
    private Follow follow;

    @BeforeEach
    void setUp() {
        alice = createAccount(1L, "alice", "example.test");
        bob = createAccount(2L, "bob", "example.test");
        charlie = createAccount(3L, "charlie", "example.test");
        
        follow = new Follow();
        follow.setAccount(alice);
        follow.setTargetAccount(bob);
    }

    @Nested
    @DisplayName("Find follow relationship")
    class FindByAccountAndTargetTests {

        @Test
        @DisplayName("Returns follow when relationship exists")
        void returnsFollowWhenExists() {
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.of(follow));
            
            Optional<Follow> result = followService.findByAccountAndTarget(alice, bob);
            
            assertThat(result).isPresent();
            assertThat(result.get().getAccount()).isEqualTo(alice);
            assertThat(result.get().getTargetAccount()).isEqualTo(bob);
        }

        @Test
        @DisplayName("Returns empty when no relationship exists")
        void returnsEmptyWhenNotExists() {
            when(followRepository.findByAccountAndTargetAccount(alice, charlie))
                    .thenReturn(Optional.empty());
            
            Optional<Follow> result = followService.findByAccountAndTarget(alice, charlie);
            
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Follow operation")
    class FollowTests {

        @Test
        @DisplayName("Creates new follow relationship")
        void createsNewFollow() {
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.empty());
            when(followRepository.save(any(Follow.class))).thenAnswer(inv -> {
                Follow f = inv.getArgument(0);
                return f;
            });
            
            Follow result = followService.follow(alice, bob);
            
            assertThat(result.getAccount()).isEqualTo(alice);
            assertThat(result.getTargetAccount()).isEqualTo(bob);
            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("Returns existing follow if already following")
        void returnsExistingFollow() {
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.of(follow));
            
            Follow result = followService.follow(alice, bob);
            
            assertThat(result).isEqualTo(follow);
        }

        @Test
        @DisplayName("Increments follower count on target account")
        void incrementsFollowerCount() {
            int initialFollowersCount = bob.getFollowersCount();
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.empty());
            when(followRepository.save(any(Follow.class))).thenAnswer(inv -> inv.getArgument(0));
            
            followService.follow(alice, bob);
            
            assertThat(bob.getFollowersCount()).isEqualTo(initialFollowersCount + 1);
            verify(accountService).save(bob);
        }

        @Test
        @DisplayName("Increments following count on source account")
        void incrementsFollowingCount() {
            int initialFollowingCount = alice.getFollowingCount();
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.empty());
            when(followRepository.save(any(Follow.class))).thenAnswer(inv -> inv.getArgument(0));
            
            followService.follow(alice, bob);
            
            assertThat(alice.getFollowingCount()).isEqualTo(initialFollowingCount + 1);
            verify(accountService).save(alice);
        }
    }

    @Nested
    @DisplayName("Unfollow operation")
    class UnfollowTests {

        @Test
        @DisplayName("Removes existing follow relationship")
        void removesExistingFollow() {
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.of(follow));
            
            followService.unfollow(alice, bob);
            
            verify(followRepository).delete(follow);
        }

        @Test
        @DisplayName("Does nothing when no relationship exists")
        void doesNothingWhenNotFollowing() {
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.empty());
            
            followService.unfollow(alice, bob);
            
            // No exception should be thrown
        }

        @Test
        @DisplayName("Decrements follower count on target account")
        void decrementsFollowerCount() {
            bob.setFollowersCount(5);
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.of(follow));
            
            followService.unfollow(alice, bob);
            
            assertThat(bob.getFollowersCount()).isEqualTo(4);
            verify(accountService).save(bob);
        }

        @Test
        @DisplayName("Decrements following count on source account")
        void decrementsFollowingCount() {
            alice.setFollowingCount(3);
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.of(follow));
            
            followService.unfollow(alice, bob);
            
            assertThat(alice.getFollowingCount()).isEqualTo(2);
            verify(accountService).save(alice);
        }

        @Test
        @DisplayName("Does not decrement count below zero")
        void doesNotDecrementBelowZero() {
            alice.setFollowingCount(0);
            bob.setFollowersCount(0);
            when(followRepository.findByAccountAndTargetAccount(alice, bob))
                    .thenReturn(Optional.of(follow));
            
            followService.unfollow(alice, bob);
            
            assertThat(alice.getFollowingCount()).isEqualTo(0);
            assertThat(bob.getFollowersCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Find followers")
    class FindFollowersTests {

        @Test
        @DisplayName("Returns followers for account")
        void returnsFollowersForAccount() {
            Follow follow1 = new Follow();
            follow1.setAccount(alice);
            follow1.setTargetAccount(bob);
            Follow follow2 = new Follow();
            follow2.setAccount(charlie);
            follow2.setTargetAccount(bob);
            
            when(followRepository.findByTargetAccount(bob))
                    .thenReturn(List.of(follow1, follow2));
            
            List<Follow> result = followService.findByTargetAccount(bob);
            
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(follow1, follow2);
        }

        @Test
        @DisplayName("Returns empty list for account with no followers")
        void returnsEmptyForNoFollowers() {
            when(followRepository.findByTargetAccount(alice))
                    .thenReturn(List.of());
            
            List<Follow> result = followService.findByTargetAccount(alice);
            
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find following")
    class FindFollowingTests {

        @Test
        @DisplayName("Returns accounts being followed")
        void returnsFollowingForAccount() {
            Follow follow1 = new Follow();
            follow1.setAccount(alice);
            follow1.setTargetAccount(bob);
            Follow follow2 = new Follow();
            follow2.setAccount(alice);
            follow2.setTargetAccount(charlie);
            
            when(followRepository.findByAccount(alice))
                    .thenReturn(List.of(follow1, follow2));
            
            List<Follow> result = followService.findByAccount(alice);
            
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(follow1, follow2);
        }

        @Test
        @DisplayName("Returns empty list for account following no one")
        void returnsEmptyForNotFollowing() {
            when(followRepository.findByAccount(alice))
                    .thenReturn(List.of());
            
            List<Follow> result = followService.findByAccount(alice);
            
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Save follow")
    class SaveTests {

        @Test
        @DisplayName("Saves follow to repository")
        void savesFollowToRepository() {
            when(followRepository.save(follow)).thenReturn(follow);
            
            Follow result = followService.save(follow);
            
            assertThat(result).isEqualTo(follow);
            verify(followRepository).save(follow);
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
}
