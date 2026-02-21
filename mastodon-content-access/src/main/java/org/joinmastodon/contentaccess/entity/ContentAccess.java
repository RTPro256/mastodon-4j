package org.joinmastodon.contentaccess.entity;

import jakarta.persistence.*;
import org.joinmastodon.core.entity.Status;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Content access configuration for a status.
 */
@Entity
@Table(name = "content_access")
public class ContentAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "status_id", nullable = false, unique = true)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    private AccessLevel accessLevel = AccessLevel.PUBLIC;

    @OneToMany(mappedBy = "contentAccess", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ContentPermission> permissions = new HashSet<>();

    @Column(name = "server_only")
    private Boolean serverOnly = false;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors
    public ContentAccess() {}

    public ContentAccess(Status status, AccessLevel accessLevel) {
        this.status = status;
        this.accessLevel = accessLevel;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Set<ContentPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<ContentPermission> permissions) {
        this.permissions = permissions;
    }

    public Boolean getServerOnly() {
        return serverOnly;
    }

    public void setServerOnly(Boolean serverOnly) {
        this.serverOnly = serverOnly;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Check if access has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * Add a permission.
     */
    public void addPermission(ContentPermission permission) {
        permissions.add(permission);
        permission.setContentAccess(this);
    }

    /**
     * Remove a permission.
     */
    public void removePermission(ContentPermission permission) {
        permissions.remove(permission);
        permission.setContentAccess(null);
    }

    /**
     * Access levels for content.
     */
    public enum AccessLevel {
        /**
         * Publicly visible content.
         */
        PUBLIC,

        /**
         * Only visible to users on this instance.
         */
        INSTANCE_ONLY,

        /**
         * Only visible to followers.
         */
        FOLLOWERS_ONLY,

        /**
         * Only visible to explicitly granted users.
         */
        PRIVATE,

        /**
         * Only visible to mentioned users.
         */
        DIRECT
    }
}
