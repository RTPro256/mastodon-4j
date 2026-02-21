package org.joinmastodon.core.repository;

import org.joinmastodon.core.entity.EmailConfirmationToken;
import org.joinmastodon.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, Long> {
    
    /**
     * Find a token by its value.
     */
    Optional<EmailConfirmationToken> findByToken(String token);
    
    /**
     * Find the most recent valid token for a user.
     */
    Optional<EmailConfirmationToken> findFirstByUserAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            User user, Instant now);
    
    /**
     * Count unused tokens for a user.
     */
    long countByUserAndUsedFalse(User user);
    
    /**
     * Delete all expired tokens.
     */
    void deleteByExpiresAtBefore(Instant now);
    
    /**
     * Delete all tokens for a user.
     */
    void deleteByUser(User user);
}
