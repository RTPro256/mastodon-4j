package org.joinmastodon.torrent.torrentfile;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Metadata about a torrent file.
 */
public class TorrentFileInfo {

    private final String infoHash;
    private final String name;
    private final long totalSize;
    private final int pieceSize;
    private final int pieceCount;
    private final List<String> trackers;
    private final List<TorrentFileEntry> files;
    private final Instant createdAt;
    private final String createdBy;
    private final String comment;
    private final boolean privateTorrent;

    public TorrentFileInfo(String infoHash, String name, long totalSize, int pieceSize,
                          int pieceCount, List<String> trackers, List<TorrentFileEntry> files,
                          Instant createdAt, String createdBy, String comment, boolean privateTorrent) {
        this.infoHash = infoHash;
        this.name = name;
        this.totalSize = totalSize;
        this.pieceSize = pieceSize;
        this.pieceCount = pieceCount;
        this.trackers = trackers != null ? new ArrayList<>(trackers) : new ArrayList<>();
        this.files = files != null ? new ArrayList<>(files) : new ArrayList<>();
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.comment = comment;
        this.privateTorrent = privateTorrent;
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

    public int getPieceSize() {
        return pieceSize;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public List<String> getTrackers() {
        return new ArrayList<>(trackers);
    }

    public List<TorrentFileEntry> getFiles() {
        return new ArrayList<>(files);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getComment() {
        return comment;
    }

    public boolean isPrivateTorrent() {
        return privateTorrent;
    }

    /**
     * Check if this is a single-file torrent.
     */
    public boolean isSingleFile() {
        return files.size() <= 1;
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

    /**
     * Represents a file entry in a multi-file torrent.
     */
    public static class TorrentFileEntry {
        private final long length;
        private final List<String> path;
        private final Path filePath;

        public TorrentFileEntry(long length, List<String> path, Path filePath) {
            this.length = length;
            this.path = path != null ? new ArrayList<>(path) : new ArrayList<>();
            this.filePath = filePath;
        }

        public long getLength() {
            return length;
        }

        public List<String> getPath() {
            return new ArrayList<>(path);
        }

        public Path getFilePath() {
            return filePath;
        }
    }
}
