package org.joinmastodon.core.repository;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByAccount(Account account);
    
    /**
     * Find all users pending approval.
     */
    List<User> findByApprovalRequiredTrueAndApprovedFalse();
    
    /**
     * Count users pending approval.
     */
    long countByApprovalRequiredTrueAndApprovedFalse();
    
    /**
     * Find users by role.
     */
    List<User> findByRole(User.Role role);
    
    /**
     * Find unconfirmed users created before a given time.
     */
    List<User> findByConfirmedFalseAndCreatedAtBefore(java.time.Instant createdBefore);
}
