package org.joinmastodon.federationindex.entity;

import jakarta.persistence.*;
import org.joinmastodon.federationindex.entity.ServerIndex.AccessPolicy;
import org.joinmastodon.federationindex.entity.ServerIndex.ContentRating;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Index entry for a federated server.
 */
@Entity
@Table(name = "server_index")
public class ServerIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String domain;

    // Basic Information
    @Column(name = "display_name")
    private String displayName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String version;

    // Availability Tracking
    @Column(name = "last_seen_at")
    private Instant lastSeenAt;
    
    @Column(name = "availability_score", precision = 3, scale = 2)
    private BigDecimal availabilityScore = BigDecimal.ONE;
    
    @Column(name = "is_online")
    private Boolean isOnline = true;

    // Content Classification
    @Enumerated(EnumType.STRING)
    @Column(name = "content_rating")
    private ContentRating contentRating;
    
    @Column(name = "content_description", columnDefinition = "TEXT")
    private String contentDescription;

    // Internal Rating (our server's view)
    @Column(name = "internal_rating", precision = 2, scale = 1)
    private BigDecimal internalRating;
    
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    // Aggregated External Rating
    @Column(name = "external_rating", precision = 2, scale = 1)
    private BigDecimal externalRating;
    
    @Column(name = "external_rating_count")
    private Integer externalRatingCount = 0;

    // Access Policy
    @Enumerated(EnumType.STRING)
    @Column(name = "access_policy")
    private AccessPolicy accessPolicy = AccessPolicy.OPEN;
    
    @Column(name = "restriction_reason", columnDefinition = "TEXT")
    private String restrictionReason;

    // Metadata
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
    public ServerIndex() {}

    public ServerIndex(String domain) {
        this.domain = domain;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public BigDecimal getAvailabilityScore() {
        return availabilityScore;
    }

    public void setAvailabilityScore(BigDecimal availabilityScore) {
        this.availabilityScore = availabilityScore;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public ContentRating getContentRating() {
        return contentRating;
    }

    public void setContentRating(ContentRating contentRating) {
        this.contentRating = contentRating;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public BigDecimal getInternalRating() {
        return internalRating;
    }

    public void setInternalRating(BigDecimal internalRating) {
        this.internalRating = internalRating;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public BigDecimal getExternalRating() {
        return externalRating;
    }

    public void setExternalRating(BigDecimal externalRating) {
        this.externalRating = externalRating;
    }

    public Integer getExternalRatingCount() {
        return externalRatingCount;
    }

    public void setExternalRatingCount(Integer externalRatingCount) {
        this.externalRatingCount = externalRatingCount;
    }

    public AccessPolicy getAccessPolicy() {
        return accessPolicy;
    }

    public void setAccessPolicy(AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    public String getRestrictionReason() {
        return restrictionReason;
    }

    public void setRestrictionReason(String restrictionReason) {
        this.restrictionReason = restrictionReason;
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
     * Content rating classification.
     */
    public enum ContentRating {
        G,      // General audiences
        PG,     // Parental guidance suggested
        PG_13,  // Parents strongly cautioned
        R,      // Restricted
        NC_17,  // Adults only
        NSFW    // Not safe for work
    }

    /**
     * Access policy for the server.
     */
    public enum AccessPolicy {
        OPEN,       // Open federation
        RESTRICTED, // Restricted access
        BLOCKED     // Blocked from federation
    }
}
