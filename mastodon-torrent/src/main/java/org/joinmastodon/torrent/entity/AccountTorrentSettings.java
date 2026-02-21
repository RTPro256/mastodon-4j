package org.joinmastodon.torrent.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * User preferences for torrent sharing.
 */
@Entity
@Table(name = "account_torrent_settings")
public class AccountTorrentSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The account ID these settings belong to.
     */
    @Column(name = "account_id", unique = true, nullable = false)
    private Long accountId;

    /**
     * Whether the user has opted in to share content via torrents.
     */
    @Column(name = "share_via_torrent")
    private boolean shareViaTorrent = false;

    /**
     * Whether to automatically seed uploaded content.
     */
    @Column(name = "auto_seed_uploads")
    private boolean autoSeedUploads = true;

    /**
     * Maximum seeding ratio before stopping.
     */
    @Column(name = "max_seeding_ratio")
    private Double maxSeedingRatio = 2.0;

    /**
     * Maximum seeding time in hours before stopping.
     */
    @Column(name = "max_seeding_hours")
    private Integer maxSeedingHours = 168; // 1 week

    /**
     * Maximum upload rate (e.g., "1MB", "500KB").
     */
    @Column(name = "max_upload_rate", length = 20)
    private String maxUploadRate = "1MB";

    /**
     * Whether to hide peer ID attribution (anonymous seeding).
     */
    @Column(name = "anonymous_seeding")
    private boolean anonymousSeeding = false;

    /**
     * When these settings were created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * When these settings were last updated.
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public boolean isShareViaTorrent() {
        return shareViaTorrent;
    }

    public void setShareViaTorrent(boolean shareViaTorrent) {
        this.shareViaTorrent = shareViaTorrent;
    }

    public boolean isAutoSeedUploads() {
        return autoSeedUploads;
    }

    public void setAutoSeedUploads(boolean autoSeedUploads) {
        this.autoSeedUploads = autoSeedUploads;
    }

    public Double getMaxSeedingRatio() {
        return maxSeedingRatio;
    }

    public void setMaxSeedingRatio(Double maxSeedingRatio) {
        this.maxSeedingRatio = maxSeedingRatio;
    }

    public Integer getMaxSeedingHours() {
        return maxSeedingHours;
    }

    public void setMaxSeedingHours(Integer maxSeedingHours) {
        this.maxSeedingHours = maxSeedingHours;
    }

    public String getMaxUploadRate() {
        return maxUploadRate;
    }

    public void setMaxUploadRate(String maxUploadRate) {
        this.maxUploadRate = maxUploadRate;
    }

    public boolean isAnonymousSeeding() {
        return anonymousSeeding;
    }

    public void setAnonymousSeeding(boolean anonymousSeeding) {
        this.anonymousSeeding = anonymousSeeding;
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
}
