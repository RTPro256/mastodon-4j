package org.joinmastodon.torrent.tracker;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for tracker clients.
 * 
 * Tracker clients communicate with BitTorrent trackers to:
 * - Announce the client's presence and get peer lists
 * - Report download/upload statistics
 * - Support the UDP, HTTP, and WebSocket tracker protocols
 */
public abstract class TrackerClient {

    protected final int timeoutMs;
    protected final int retryCount;

    public TrackerClient() {
        this(15000, 3); // Default: 15 second timeout, 3 retries
    }

    public TrackerClient(int timeoutMs, int retryCount) {
        this.timeoutMs = timeoutMs;
        this.retryCount = retryCount;
    }

    /**
     * Announce to the tracker.
     *
     * @param trackerUrl The tracker URL
     * @param infoHash   The 20-byte infohash
     * @param peerId     The 20-byte peer ID
     * @param port       The listening port
     * @param uploaded   Total bytes uploaded
     * @param downloaded Total bytes downloaded
     * @param left       Bytes remaining to download
     * @param event      The announce event
     * @return A future containing the announce result
     */
    public abstract CompletableFuture<TrackerAnnounceResult> announce(
            String trackerUrl,
            byte[] infoHash,
            byte[] peerId,
            int port,
            long uploaded,
            long downloaded,
            long left,
            AnnounceEvent event);

    /**
     * Announce with default event (NONE).
     */
    public CompletableFuture<TrackerAnnounceResult> announce(
            String trackerUrl,
            byte[] infoHash,
            byte[] peerId,
            int port,
            long uploaded,
            long downloaded,
            long left) {
        return announce(trackerUrl, infoHash, peerId, port, uploaded, downloaded, left, AnnounceEvent.NONE);
    }

    /**
     * Scrape the tracker for statistics.
     *
     * @param trackerUrl The tracker URL
     * @param infoHashes The infohashes to scrape
     * @return A future containing the scrape result
     */
    public abstract CompletableFuture<ScrapeResult> scrape(
            String trackerUrl,
            byte[]... infoHashes);

    /**
     * Check if this client supports the given tracker URL.
     *
     * @param trackerUrl The tracker URL
     * @return true if supported
     */
    public abstract boolean supports(String trackerUrl);

    /**
     * Announce event types.
     */
    public enum AnnounceEvent {
        NONE(0),
        COMPLETED(1),
        STARTED(2),
        STOPPED(3);

        private final int code;

        AnnounceEvent(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static AnnounceEvent fromCode(int code) {
            return switch (code) {
                case 0 -> NONE;
                case 1 -> COMPLETED;
                case 2 -> STARTED;
                case 3 -> STOPPED;
                default -> NONE;
            };
        }
    }

    /**
     * Result of a scrape operation.
     */
    public record ScrapeResult(
            String trackerUrl,
            Status status,
            String errorMessage,
            java.util.List<ScrapeEntry> entries
    ) {
        public enum Status {
            SUCCESS,
            FAILED,
            TIMEOUT
        }
    }

    /**
     * Scrape entry for a single infohash.
     */
    public record ScrapeEntry(
            byte[] infoHash,
            int seeders,
            int completed,
            int leechers
    ) {}
}
