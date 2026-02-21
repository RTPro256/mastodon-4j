package org.joinmastodon.torrent.core;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Represents torrent metadata and information.
 */
public class TorrentInfo {

    private final String infoHash;
    private final String name;
    private final long totalSize;
    private final int pieceCount;
    private final int pieceLength;
    private final List<String> trackers;
    private final List<String> files;
    private final Instant createdAt;
    private final String createdBy;

    public TorrentInfo(String infoHash, String name, long totalSize, int pieceCount,
                      int pieceLength, List<String> trackers, List<String> files,
                      Instant createdAt, String createdBy) {
        this.infoHash = infoHash;
        this.name = name;
        this.totalSize = totalSize;
        this.pieceCount = pieceCount;
        this.pieceLength = pieceLength;
        this.trackers = trackers;
        this.files = files;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public String getInfoHash() {
        return infoHash;
    }

    public String getName() {
        return name;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public int getPieceLength() {
        return pieceLength;
    }

    public List<String> getTrackers() {
        return trackers;
    }

    public List<String> getFiles() {
        return files;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Get human-readable size.
     */
    public String getHumanReadableSize() {
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
