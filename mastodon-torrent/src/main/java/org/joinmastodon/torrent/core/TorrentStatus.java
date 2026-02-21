package org.joinmastodon.torrent.core;

/**
 * Status of a torrent download/seeding.
 */
public class TorrentStatus {

    private final String infoHash;
    private final State state;
    private final long downloadedBytes;
    private final long uploadedBytes;
    private final long totalBytes;
    private final int downloadRate;
    private final int uploadRate;
    private final double progress;
    private final int seeds;
    private final int peers;
    private final double ratio;

    public TorrentStatus(String infoHash, State state, long downloadedBytes, long uploadedBytes,
                        long totalBytes, int downloadRate, int uploadRate, int seeds, int peers) {
        this.infoHash = infoHash;
        this.state = state;
        this.downloadedBytes = downloadedBytes;
        this.uploadedBytes = uploadedBytes;
        this.totalBytes = totalBytes;
        this.downloadRate = downloadRate;
        this.uploadRate = uploadRate;
        this.seeds = seeds;
        this.peers = peers;
        this.progress = totalBytes > 0 ? (double) downloadedBytes / totalBytes : 0;
        this.ratio = downloadedBytes > 0 ? (double) uploadedBytes / downloadedBytes : 0;
    }

    public String getInfoHash() {
        return infoHash;
    }

    public State getState() {
        return state;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public long getUploadedBytes() {
        return uploadedBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public int getDownloadRate() {
        return downloadRate;
    }

    public int getUploadRate() {
        return uploadRate;
    }

    public double getProgress() {
        return progress;
    }

    public int getSeeds() {
        return seeds;
    }

    public int getPeers() {
        return peers;
    }

    public double getRatio() {
        return ratio;
    }

    /**
     * Get progress as percentage.
     */
    public double getProgressPercent() {
        return progress * 100;
    }

    /**
     * Check if download is complete.
     */
    public boolean isComplete() {
        return downloadedBytes >= totalBytes && totalBytes > 0;
    }

    /**
     * Get human-readable download rate.
     */
    public String getHumanReadableDownloadRate() {
        return formatRate(downloadRate);
    }

    /**
     * Get human-readable upload rate.
     */
    public String getHumanReadableUploadRate() {
        return formatRate(uploadRate);
    }

    private String formatRate(int bytesPerSecond) {
        if (bytesPerSecond < 1024) {
            return bytesPerSecond + " B/s";
        } else if (bytesPerSecond < 1024 * 1024) {
            return String.format("%.2f KB/s", bytesPerSecond / 1024.0);
        } else {
            return String.format("%.2f MB/s", bytesPerSecond / (1024.0 * 1024));
        }
    }

    /**
     * Torrent states.
     */
    public enum State {
        /**
         * Torrent is queued for download.
         */
        QUEUED,

        /**
         * Torrent is checking files.
         */
        CHECKING,

        /**
         * Torrent is downloading metadata.
         */
        DOWNLOADING_METADATA,

        /**
         * Torrent is downloading.
         */
        DOWNLOADING,

        /**
         * Torrent is finished downloading.
         */
        FINISHED,

        /**
         * Torrent is seeding.
         */
        SEEDING,

        /**
         * Torrent is paused.
         */
        PAUSED,

        /**
         * Torrent has an error.
         */
        ERROR
    }
}
