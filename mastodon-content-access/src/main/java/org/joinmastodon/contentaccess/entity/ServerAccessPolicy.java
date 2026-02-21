package org.joinmastodon.contentaccess.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Access policy for external servers.
 */
@Entity
@Table(name = "server_access_policies")
public class ServerAccessPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_domain", nullable = false, unique = true)
    private String targetDomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy", nullable = false)
    private AccessPolicy policy = AccessPolicy.ALLOWED;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

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
    public ServerAccessPolicy() {}

    public ServerAccessPolicy(String targetDomain, AccessPolicy policy) {
        this.targetDomain = targetDomain;
        this.policy = policy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTargetDomain() {
        return targetDomain;
    }

    public void setTargetDomain(String targetDomain) {
        this.targetDomain = targetDomain;
    }

    public AccessPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(AccessPolicy policy) {
        this.policy = policy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Check if policy has expired.
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if access is allowed.
     */
    public boolean isAccessAllowed() {
        if (isExpired()) {
            return true; // Expired policies default to allowed
        }
        return policy == AccessPolicy.ALLOWED;
    }

    /**
     * Access policy types.
     */
    public enum AccessPolicy {
        /**
         * Access is allowed.
         */
        ALLOWED,

        /**
         * Access is restricted (limited functionality).
         */
        RESTRICTED,

        /**
         * Access is blocked.
         */
        BLOCKED
    }
}
