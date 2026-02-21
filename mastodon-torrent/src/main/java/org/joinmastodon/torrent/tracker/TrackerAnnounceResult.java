package org.joinmastodon.torrent.tracker;

import java.time.Instant;
import java.util.List;

/**
 * Result of a tracker announce operation.
 */
public class TrackerAnnounceResult {

    private final String trackerUrl;
    private final Status status;
    private final int seeders;
    private final int leechers;
    private final int interval;
    private final int minInterval;
    private final List<PeerInfo> peers;
    private final String errorMessage;
    private final Instant announcedAt;
    private final long responseTimeMs;

    public TrackerAnnounceResult(String trackerUrl, Status status, int seeders, int leechers,
                                 int interval, int minInterval, List<PeerInfo> peers,
                                 String errorMessage, Instant announcedAt, long responseTimeMs) {
        this.trackerUrl = trackerUrl;
        this.status = status;
        this.seeders = seeders;
        this.leechers = leechers;
        this.interval = interval;
        this.minInterval = minInterval;
        this.peers = peers;
        this.errorMessage = errorMessage;
        this.announcedAt = announcedAt;
        this.responseTimeMs = responseTimeMs;
    }

    /**
     * Create a successful result.
     */
    public static TrackerAnnounceResult success(String trackerUrl, int seeders, int leechers,
                                                int interval, List<PeerInfo> peers, long responseTimeMs) {
        return new TrackerAnnounceResult(trackerUrl, Status.SUCCESS, seeders, leechers,
                interval, 0, peers, null, Instant.now(), responseTimeMs);
    }

    /**
     * Create a failed result.
     */
    public static TrackerAnnounceResult failure(String trackerUrl, String errorMessage, long responseTimeMs) {
        return new TrackerAnnounceResult(trackerUrl, Status.FAILED, 0, 0, 0, 0,
                List.of(), errorMessage, Instant.now(), responseTimeMs);
    }

    /**
     * Create a timeout result.
     */
    public static TrackerAnnounceResult timeout(String trackerUrl, long responseTimeMs) {
        return new TrackerAnnounceResult(trackerUrl, Status.TIMEOUT, 0, 0, 0, 0,
                List.of(), "Tracker request timed out", Instant.now(), responseTimeMs);
    }

    public String getTrackerUrl() {
        return trackerUrl;
    }

    public Status getStatus() {
        return status;
    }

    public int getSeeders() {
        return seeders;
    }

    public int getLeechers() {
        return leechers;
    }

    public int getInterval() {
        return interval;
    }

    public int getMinInterval() {
        return minInterval;
    }

    public List<PeerInfo> getPeers() {
        return peers;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getAnnouncedAt() {
        return announcedAt;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    /**
     * Status of the announce operation.
     */
    public enum Status {
        SUCCESS,
        FAILED,
        TIMEOUT
    }

    /**
     * Information about a peer.
     */
    public record PeerInfo(String ip, int port, byte[] peerId) {
        
        public PeerInfo(String ip, int port) {
            this(ip, port, null);
        }
        
        /**
         * Get the peer address as "ip:port".
         */
        public String getAddress() {
            return ip + ":" + port;
        }
        
        /**
         * Get the peer ID as a hex string.
         */
        public String getPeerIdHex() {
            if (peerId == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : peerId) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        }
    }
}
