package org.joinmastodon.core.service;

import org.joinmastodon.core.entity.User;
import org.joinmastodon.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing the account approval workflow.
 * When approval is required, new accounts must be approved by an admin before they can be used.
 */
@Service
public class AccountApprovalService {
    
    private static final Logger log = LoggerFactory.getLogger(AccountApprovalService.class);
    
    private final UserRepository userRepository;
    
    @Value("${mastodon.approval.required:false}")
    private boolean approvalRequired;
    
    @Value("${mastodon.approval.default-approved:true}")
    private boolean defaultApproved;
    
    public AccountApprovalService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Check if account approval is required for new registrations.
     */
    public boolean isApprovalRequired() {
        return approvalRequired;
    }
    
    /**
     * Check if a user needs approval.
     */
    public boolean needsApproval(User user) {
        return user.isApprovalRequired() && !user.isApproved();
    }
    
    /**
     * Set up approval requirements for a new user.
     * This should be called during registration.
     * 
     * @param user The new user
     */
    @Transactional
    public void setupApprovalForNewUser(User user) {
        if (approvalRequired) {
            user.setApprovalRequired(true);
            user.setApproved(false);
            log.info("New user {} requires approval", user.getEmail());
        } else {
            user.setApprovalRequired(false);
            user.setApproved(defaultApproved);
            log.debug("New user {} auto-approved (approval not required)", user.getEmail());
        }
        userRepository.save(user);
    }
    
    /**
     * Approve a user account.
     * 
     * @param userId The user ID to approve
     * @param adminUser The admin performing the approval
     * @return true if approval was successful
     */
    @Transactional
    public boolean approveUser(Long userId, User adminUser) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for approval: {}", userId);
            return false;
        }
        
        if (user.isApproved()) {
            log.debug("User {} is already approved", user.getEmail());
            return true;
        }
        
        user.setApproved(true);
        userRepository.save(user);
        
        log.info("User {} approved by admin {}", user.getEmail(), adminUser.getEmail());
        return true;
    }
    
    /**
     * Reject a user account (delete the account).
     * 
     * @param userId The user ID to reject
     * @param adminUser The admin performing the rejection
     * @param reason The reason for rejection
     * @return true if rejection was successful
     */
    @Transactional
    public boolean rejectUser(Long userId, User adminUser, String reason) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for rejection: {}", userId);
            return false;
        }
        
        log.info("User {} rejected by admin {}. Reason: {}", 
                user.getEmail(), adminUser.getEmail(), reason);
        
        // Delete the user account
        userRepository.delete(user);
        
        return true;
    }
    
    /**
     * Get all users pending approval.
     * 
     * @return List of users waiting for approval
     */
    public List<User> getPendingApprovals() {
        return userRepository.findByApprovalRequiredTrueAndApprovedFalse();
    }
    
    /**
     * Count users pending approval.
     */
    public long countPendingApprovals() {
        return userRepository.countByApprovalRequiredTrueAndApprovedFalse();
    }
}
