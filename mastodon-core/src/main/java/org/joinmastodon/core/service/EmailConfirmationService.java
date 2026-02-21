package org.joinmastodon.core.service;

import org.joinmastodon.core.entity.EmailConfirmationToken;
import org.joinmastodon.core.entity.User;
import org.joinmastodon.core.repository.EmailConfirmationTokenRepository;
import org.joinmastodon.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

/**
 * Service for managing email confirmation tokens and the confirmation process.
 */
@Service
public class EmailConfirmationService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailConfirmationService.class);
    
    private final EmailConfirmationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${mastodon.email.confirmation-ttl-hours:24}")
    private int confirmationTtlHours;
    
    @Value("${mastodon.email.confirmation-required:true}")
    private boolean confirmationRequired;
    
    public EmailConfirmationService(EmailConfirmationTokenRepository tokenRepository,
                                    UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Generate a new confirmation token for a user.
     * 
     * @param user The user to generate a token for
     * @return The generated token
     */
    @Transactional
    public EmailConfirmationToken generateToken(User user) {
        // Invalidate any existing tokens for this user
        tokenRepository.deleteByUser(user);
        
        // Generate a secure random token
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = HexFormat.of().formatHex(tokenBytes);
        
        EmailConfirmationToken confirmationToken = new EmailConfirmationToken();
        confirmationToken.setUser(user);
        confirmationToken.setToken(token);
        confirmationToken.setCreatedAt(Instant.now());
        confirmationToken.setExpiresAt(Instant.now().plus(confirmationTtlHours, ChronoUnit.HOURS));
        confirmationToken.setUsed(false);
        
        return tokenRepository.save(confirmationToken);
    }
    
    /**
     * Confirm a user's email using the provided token.
     * 
     * @param token The confirmation token
     * @return true if confirmation was successful, false otherwise
     */
    @Transactional
    public boolean confirmEmail(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Empty confirmation token provided");
            return false;
        }
        
        EmailConfirmationToken confirmationToken = tokenRepository.findByToken(token)
                .orElse(null);
        
        if (confirmationToken == null) {
            log.warn("Confirmation token not found: {}", token.substring(0, 8) + "...");
            return false;
        }
        
        if (!confirmationToken.isValid()) {
            log.warn("Confirmation token is invalid (used: {}, expired: {})", 
                    confirmationToken.isUsed(), 
                    confirmationToken.isExpired());
            return false;
        }
        
        // Mark token as used
        confirmationToken.setUsed(true);
        confirmationToken.setUsedAt(Instant.now());
        tokenRepository.save(confirmationToken);
        
        // Mark user as confirmed
        User user = confirmationToken.getUser();
        user.setConfirmed(true);
        user.setConfirmedAt(Instant.now());
        userRepository.save(user);
        
        log.info("Email confirmed for user: {}", user.getEmail());
        return true;
    }
    
    /**
     * Resend confirmation email for a user.
     * 
     * @param user The user to resend confirmation for
     * @return The new token, or null if user is already confirmed
     */
    @Transactional
    public EmailConfirmationToken resendConfirmation(User user) {
        if (user.isConfirmed()) {
            log.debug("User {} is already confirmed", user.getEmail());
            return null;
        }
        
        // Check rate limiting - max 3 pending tokens
        long pendingCount = tokenRepository.countByUserAndUsedFalse(user);
        if (pendingCount >= 3) {
            log.warn("Too many pending confirmation tokens for user: {}", user.getEmail());
            // Clean up old tokens and generate a new one
            tokenRepository.deleteByUser(user);
        }
        
        return generateToken(user);
    }
    
    /**
     * Check if email confirmation is required.
     */
    public boolean isConfirmationRequired() {
        return confirmationRequired;
    }
    
    /**
     * Check if a user needs email confirmation.
     */
    public boolean needsConfirmation(User user) {
        return confirmationRequired && !user.isConfirmed();
    }
    
    /**
     * Clean up expired tokens.
     * Should be called periodically by a scheduled job.
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(Instant.now());
        log.debug("Cleaned up expired email confirmation tokens");
    }
}
