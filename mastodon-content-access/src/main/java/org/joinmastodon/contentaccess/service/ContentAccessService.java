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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing content access control.
 */
@Service
public class ContentAccessService {

    private static final Logger log = LoggerFactory.getLogger(ContentAccessService.class);

    private final ContentAccessRepository contentAccessRepository;
    private final StatusRepository statusRepository;
    private final AccountRepository accountRepository;

    public ContentAccessService(ContentAccessRepository contentAccessRepository,
                               StatusRepository statusRepository,
                               AccountRepository accountRepository) {
        this.contentAccessRepository = contentAccessRepository;
        this.statusRepository = statusRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Create content access for a status.
     */
    @Transactional
    public ContentAccess createContentAccess(Long statusId, AccessLevel accessLevel) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Status not found: " + statusId));

        ContentAccess contentAccess = new ContentAccess(status, accessLevel);
        return contentAccessRepository.save(contentAccess);
    }

    /**
     * Get content access for a status.
     */
    public Optional<ContentAccess> getContentAccess(Long statusId) {
        return contentAccessRepository.findByStatusId(statusId);
    }

    /**
     * Update access level for content.
     */
    @Transactional
    public ContentAccess updateAccessLevel(Long statusId, AccessLevel newLevel) {
        ContentAccess contentAccess = contentAccessRepository.findByStatusId(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Content access not found for status: " + statusId));

        contentAccess.setAccessLevel(newLevel);
        return contentAccessRepository.save(contentAccess);
    }

    /**
     * Grant permission to an account for content.
     */
    @Transactional
    public ContentPermission grantPermission(Long statusId, Long granteeAccountId, 
                                             Permission permission, Long grantedBy) {
        ContentAccess contentAccess = contentAccessRepository.findByStatusId(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Content access not found for status: " + statusId));

        Account grantee = accountRepository.findById(granteeAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + granteeAccountId));

        ContentPermission contentPermission = new ContentPermission(contentAccess, grantee, permission);
        contentPermission.setGrantedBy(grantedBy);
        contentAccess.addPermission(contentPermission);

        contentAccessRepository.save(contentAccess);
        log.info("Granted {} permission to account {} for status {}", permission, granteeAccountId, statusId);

        return contentPermission;
    }

    /**
     * Revoke permission from an account.
     */
    @Transactional
    public void revokePermission(Long statusId, Long granteeAccountId) {
        ContentAccess contentAccess = contentAccessRepository.findByStatusId(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Content access not found for status: " + statusId));

        contentAccess.getPermissions().removeIf(p -> p.getGrantee().getId().equals(granteeAccountId));
        contentAccessRepository.save(contentAccess);
        log.info("Revoked permission from account {} for status {}", granteeAccountId, statusId);
    }

    /**
     * Check if an account can view content.
     */
    public boolean canView(Long statusId, Long viewerAccountId) {
        Optional<ContentAccess> contentAccessOpt = contentAccessRepository.findByStatusId(statusId);
        
        if (contentAccessOpt.isEmpty()) {
            // No access control = public
            return true;
        }

        ContentAccess contentAccess = contentAccessOpt.get();
        
        // Check if expired
        if (contentAccess.isExpired()) {
            return true;
        }

        AccessLevel level = contentAccess.getAccessLevel();
        
        switch (level) {
            case PUBLIC:
                return true;
            case INSTANCE_ONLY:
                // Check if viewer is on the same instance
                return isLocalAccount(viewerAccountId);
            case FOLLOWERS_ONLY:
                // Check if viewer follows the author
                return isFollower(viewerAccountId, statusId);
            case PRIVATE:
                // Check if explicit permission granted
                return hasPermission(contentAccess, viewerAccountId, Permission.VIEW);
            case DIRECT:
                // Check if mentioned
                return isMentioned(viewerAccountId, statusId);
            default:
                return false;
        }
    }

    /**
     * Check if an account has a specific permission.
     */
    public boolean hasPermission(Long statusId, Long accountId, Permission permission) {
        Optional<ContentAccess> contentAccessOpt = contentAccessRepository.findByStatusId(statusId);
        return contentAccessOpt.isPresent() && hasPermission(contentAccessOpt.get(), accountId, permission);
    }

    /**
     * Check if an account has a specific permission.
     */
    private boolean hasPermission(ContentAccess contentAccess, Long accountId, Permission permission) {
        return contentAccess.getPermissions().stream()
                .filter(p -> p.getGrantee().getId().equals(accountId))
                .filter(ContentPermission::isValid)
                .anyMatch(p -> p.getPermission().ordinal() >= permission.ordinal());
    }

    /**
     * Check if an account is local.
     */
    private boolean isLocalAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .map(a -> a.getDomain() == null)
                .orElse(false);
    }

    /**
     * Check if viewer follows the status author.
     */
    private boolean isFollower(Long viewerAccountId, Long statusId) {
        // This would need to check the Follow entity
        // For now, return false as placeholder
        return false;
    }

    /**
     * Check if viewer is mentioned in the status.
     */
    private boolean isMentioned(Long viewerAccountId, Long statusId) {
        // This would need to check the Mention entity
        // For now, return false as placeholder
        return false;
    }

    /**
     * Delete content access for a status.
     */
    @Transactional
    public void deleteContentAccess(Long statusId) {
        contentAccessRepository.deleteByStatusId(statusId);
        log.info("Deleted content access for status {}", statusId);
    }

    /**
     * Get all content with a specific access level.
     */
    public List<ContentAccess> getContentByAccessLevel(AccessLevel accessLevel) {
        return contentAccessRepository.findByAccessLevel(accessLevel);
    }
}
