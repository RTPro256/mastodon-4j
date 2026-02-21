package org.joinmastodon.contentaccess.entity;

import jakarta.persistence.*;
import org.joinmastodon.core.entity.Account;

import java.time.Instant;

/**
 * Permission granted to a specific account for content access.
 */
@Entity
@Table(name = "content_permissions", 
        uniqueConstraints = @UniqueConstraint(columnNames = {"content_access_id", "grantee_id"}))
public class ContentPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_access_id", nullable = false)
    private ContentAccess contentAccess;

    @ManyToOne
    @JoinColumn(name = "grantee_id", nullable = false)
    private Account grantee;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Permission permission = Permission.VIEW;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    // Constructors
    public ContentPermission() {}

    public ContentPermission(ContentAccess contentAccess, Account grantee, Permission permission) {
        this.contentAccess = contentAccess;
        this.grantee = grantee;
        this.permission = permission;
        this.grantedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (grantedAt == null) {
            grantedAt = Instant.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContentAccess getContentAccess() {
        return contentAccess;
    }

    public void setContentAccess(ContentAccess contentAccess) {
        this.contentAccess = contentAccess;
    }

    public Account getGrantee() {
        return grantee;
    }

    public void setGrantee(Account grantee) {
        this.grantee = grantee;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Long getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(Long grantedBy) {
        this.grantedBy = grantedBy;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(Instant grantedAt) {
        this.grantedAt = grantedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Check if permission has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if permission is still valid.
     */
    public boolean isValid() {
        return !isExpired();
    }

    /**
     * Permission types.
     */
    public enum Permission {
        /**
         * Can view the content.
         */
        VIEW,

        /**
         * Can comment on the content.
         */
        COMMENT,

        /**
         * Can share/boost the content.
         */
        SHARE,

        /**
         * Full administrative access.
         */
        ADMIN
    }
}
