package org.joinmastodon.torrent.api.dto;

import org.joinmastodon.torrent.entity.SeedingStatus;
import org.joinmastodon.torrent.entity.SharedTorrent;

import java.time.Instant;
import java.util.List;

/**
 * Response containing torrent information.
 */
public class TorrentResponse {

    private String id;
    private String infoHash;
    private String name;
    private long totalSize;
    private String humanReadableSize;
    private int pieceSize;
    private int pieceCount;
    private String magnetLink;
    private String torrentUrl;
    private List<String> trackers;
    private boolean seedingEnabled;
    private SeedingStatus seedingStatus;
    private TorrentStatistics statistics;
    private Instant createdAt;

    public static TorrentResponse from(SharedTorrent torrent, String baseUrl) {
        TorrentResponse response = new TorrentResponse();
        response.setId(torrent.getId() != null ? torrent.getId().toString() : null);
        response.setInfoHash(torrent.getInfoHash());
        response.setName(torrent.getTorrentName());
        response.setTotalSize(torrent.getTotalSize() != null ? torrent.getTotalSize() : 0);
        response.setHumanReadableSize(torrent.getHumanReadableSize());
        response.setPieceSize(torrent.getPieceSize() != null ? torrent.getPieceSize() : 0);
        response.setPieceCount(torrent.getPieceCount() != null ? torrent.getPieceCount() : 0);
        response.setMagnetLink(torrent.getMagnetLink());
        response.setTorrentUrl(baseUrl + "/api/v2/torrents/" + torrent.getInfoHash() + "/torrent");
        response.setTrackers(torrent.getTrackers());
        response.setSeedingEnabled(torrent.isSeedingEnabled());
        response.setSeedingStatus(torrent.getSeedingStatus());
        response.setCreatedAt(torrent.getCreatedAt());

        TorrentStatistics stats = new TorrentStatistics();
        stats.setUploaded(torrent.getTotalUploaded() != null ? torrent.getTotalUploaded() : 0);
        stats.setDownloaded(torrent.getTotalDownloaded() != null ? torrent.getTotalDownloaded() : 0);
        stats.setRatio(torrent.getRatio());
        stats.setSeeders(torrent.getSeedCount() != null ? torrent.getSeedCount() : 0);
        stats.setLeechers(torrent.getPeerCount() != null ? torrent.getPeerCount() : 0);
        response.setStatistics(stats);

        return response;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInfoHash() {
        return infoHash;
    }

    public void setInfoHash(String infoHash) {
        this.infoHash = infoHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public String getHumanReadableSize() {
        return humanReadableSize;
    }

    public void setHumanReadableSize(String humanReadableSize) {
        this.humanReadableSize = humanReadableSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public void setPieceCount(int pieceCount) {
        this.pieceCount = pieceCount;
    }

    public String getMagnetLink() {
        return magnetLink;
    }

    public void setMagnetLink(String magnetLink) {
        this.magnetLink = magnetLink;
    }

    public String getTorrentUrl() {
        return torrentUrl;
    }

    public void setTorrentUrl(String torrentUrl) {
        this.torrentUrl = torrentUrl;
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

    public TorrentStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(TorrentStatistics statistics) {
        this.statistics = statistics;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Statistics for a torrent.
     */
    public static class TorrentStatistics {
        private long uploaded;
        private long downloaded;
        private double ratio;
        private int seeders;
        private int leechers;

        // Getters and Setters

        public long getUploaded() {
            return uploaded;
        }

        public void setUploaded(long uploaded) {
            this.uploaded = uploaded;
        }

        public long getDownloaded() {
            return downloaded;
        }

        public void setDownloaded(long downloaded) {
            this.downloaded = downloaded;
        }

        public double getRatio() {
            return ratio;
        }

        public void setRatio(double ratio) {
            this.ratio = ratio;
        }

        public int getSeeders() {
            return seeders;
        }

        public void setSeeders(int seeders) {
            this.seeders = seeders;
        }

        public int getLeechers() {
            return leechers;
        }

        public void setLeechers(int leechers) {
            this.leechers = leechers;
        }
    }
}
