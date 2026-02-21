package org.joinmastodon.torrent.entity;

import jakarta.persistence.*;
import org.joinmastodon.core.entity.MediaAttachment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a torrent shared from user content.
 */
@Entity
@Table(name = "shared_torrents", indexes = {
    @Index(name = "idx_shared_torrents_info_hash", columnList = "info_hash"),
    @Index(name = "idx_shared_torrents_account", columnList = "account_id"),
    @Index(name = "idx_shared_torrents_seeding", columnList = "seeding_status")
})
public class SharedTorrent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The media attachment this torrent was created from.
     */
    @OneToOne
    @JoinColumn(name = "media_attachment_id", unique = true)
    private MediaAttachment mediaAttachment;

    /**
     * The account that owns this torrent.
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /**
     * SHA-1 infohash (40-character hex string).
     */
    @Column(name = "info_hash", unique = true, nullable = false, length = 40)
    private String infoHash;

    /**
     * Name of the torrent (usually the file name).
     */
    @Column(name = "torrent_name")
    private String torrentName;

    /**
     * Total size of the content in bytes.
     */
    @Column(name = "total_size", nullable = false)
    private Long totalSize;

    /**
     * Size of each piece in bytes.
     */
    @Column(name = "piece_size", nullable = false)
    private Integer pieceSize;

    /**
     * Number of pieces.
     */
    @Column(name = "piece_count", nullable = false)
    private Integer pieceCount;

    /**
     * Path to the .torrent file on disk.
     */
    @Column(name = "torrent_file_path")
    private String torrentFilePath;

    /**
     * The magnet link URI.
     */
    @Column(name = "magnet_link", columnDefinition = "TEXT")
    private String magnetLink;

    /**
     * List of tracker URLs.
     */
    @ElementCollection
    @Column(name = "tracker")
    @CollectionTable(name = "shared_torrent_trackers", joinColumns = @JoinColumn(name = "shared_torrent_id"))
    private List<String> trackers = new ArrayList<>();

    /**
     * Whether seeding is enabled for this torrent.
     */
    @Column(name = "seeding_enabled")
    private boolean seedingEnabled = true;

    /**
     * Current seeding status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "seeding_status", length = 20)
    private SeedingStatus seedingStatus = SeedingStatus.INITIALIZING;

    /**
     * Total bytes uploaded.
     */
    @Column(name = "total_uploaded")
    private Long totalUploaded = 0L;

    /**
     * Total bytes downloaded.
     */
    @Column(name = "total_downloaded")
    private Long totalDownloaded = 0L;

    /**
     * Current number of seeders (from last announce).
     */
    @Column(name = "seed_count")
    private Integer seedCount = 0;

    /**
     * Current number of peers (from last announce).
     */
    @Column(name = "peer_count")
    private Integer peerCount = 0;

    /**
     * When this torrent was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * When this torrent was last updated.
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * When the last tracker announce occurred.
     */
    @Column(name = "last_announce_at")
    private Instant lastAnnounceAt;

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

    public MediaAttachment getMediaAttachment() {
        return mediaAttachment;
    }

    public void setMediaAttachment(MediaAttachment mediaAttachment) {
        this.mediaAttachment = mediaAttachment;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(String infoHash) {
        this.infoHash = infoHash;
    }

    public String getTorrentName() {
        return torrentName;
    }

    public void setTorrentName(String torrentName) {
        this.torrentName = torrentName;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getPieceSize() {
        return pieceSize;
    }

    public void setPieceSize(Integer pieceSize) {
        this.pieceSize = pieceSize;
    }

    public Integer getPieceCount() {
        return pieceCount;
    }

    public void setPieceCount(Integer pieceCount) {
        this.pieceCount = pieceCount;
    }

    public String getTorrentFilePath() {
        return torrentFilePath;
    }

    public void setTorrentFilePath(String torrentFilePath) {
        this.torrentFilePath = torrentFilePath;
    }

    public String getMagnetLink() {
        return magnetLink;
    }

    public void setMagnetLink(String magnetLink) {
        this.magnetLink = magnetLink;
    }

    public List<String> getTrackers() {
        return trackers;
    }

    public void setTrackers(List<String> trackers) {
        this.trackers = trackers;
    }

    public boolean isSeedingEnabled() {
        return seedingEnabled;
    }

    public void setSeedingEnabled(boolean seedingEnabled) {
        this.seedingEnabled = seedingEnabled;
    }

    public SeedingStatus getSeedingStatus() {
        return seedingStatus;
    }

    public void setSeedingStatus(SeedingStatus seedingStatus) {
        this.seedingStatus = seedingStatus;
    }

    public Long getTotalUploaded() {
        return totalUploaded;
    }

    public void setTotalUploaded(Long totalUploaded) {
        this.totalUploaded = totalUploaded;
    }

    public Long getTotalDownloaded() {
        return totalDownloaded;
    }

    public void setTotalDownloaded(Long totalDownloaded) {
        this.totalDownloaded = totalDownloaded;
    }

    public Integer getSeedCount() {
        return seedCount;
    }

    public void setSeedCount(Integer seedCount) {
        this.seedCount = seedCount;
    }

    public Integer getPeerCount() {
        return peerCount;
    }

    public void setPeerCount(Integer peerCount) {
        this.peerCount = peerCount;
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

    public Instant getLastAnnounceAt() {
        return lastAnnounceAt;
    }

    public void setLastAnnounceAt(Instant lastAnnounceAt) {
        this.lastAnnounceAt = lastAnnounceAt;
    }

    /**
     * Calculate the upload/download ratio.
     */
    public double getRatio() {
        if (totalDownloaded == null || totalDownloaded == 0) {
            return 0;
        }
        return (double) totalUploaded / totalDownloaded;
    }

    /**
     * Get human-readable size.
     */
    public String getHumanReadableSize() {
        if (totalSize == null) {
            return "0 B";
        }
        if (totalSize < 1024) {
            return totalSize + " B";
        } else if (totalSize < 1024 * 1024) {
            return String.format("%.2f KB", totalSize / 1024.0);
        } else if (totalSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", totalSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", totalSize / (1024.0 * 1024 * 1024));
        }
    }
}
