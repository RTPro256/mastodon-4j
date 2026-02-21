package org.joinmastodon.torrent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the torrent client.
 */
@ConfigurationProperties(prefix = "mastodon.torrent")
public class TorrentProperties {

    /**
     * Enable torrent functionality.
     */
    private boolean enabled = false;

    /**
     * Client configuration.
     */
    private ClientConfig client = new ClientConfig();

    /**
     * Storage configuration.
     */
    private StorageConfig storage = new StorageConfig();

    /**
     * DHT configuration.
     */
    private DhtConfig dht = new DhtConfig();

    /**
     * Tracker configuration.
     */
    private TrackerConfig tracker = new TrackerConfig();

    /**
     * Seeding configuration.
     */
    private SeedingConfig seeding = new SeedingConfig();

    /**
     * Federation configuration.
     */
    private FederationConfig federation = new FederationConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ClientConfig getClient() {
        return client;
    }

    public void setClient(ClientConfig client) {
        this.client = client;
    }

    public StorageConfig getStorage() {
        return storage;
    }

    public void setStorage(StorageConfig storage) {
        this.storage = storage;
    }

    public DhtConfig getDht() {
        return dht;
    }

    public void setDht(DhtConfig dht) {
        this.dht = dht;
    }

    public TrackerConfig getTracker() {
        return tracker;
    }

    public void setTracker(TrackerConfig tracker) {
        this.tracker = tracker;
    }

    public SeedingConfig getSeeding() {
        return seeding;
    }

    public void setSeeding(SeedingConfig seeding) {
        this.seeding = seeding;
    }

    public FederationConfig getFederation() {
        return federation;
    }

    public void setFederation(FederationConfig federation) {
        this.federation = federation;
    }

    /**
     * Client configuration.
     */
    public static class ClientConfig {
        private String downloadRateLimit = "10MB";
        private String uploadRateLimit = "5MB";
        private int maxConnections = 200;
        private int maxUploads = 10;

        public String getDownloadRateLimit() {
            return downloadRateLimit;
        }

        public void setDownloadRateLimit(String downloadRateLimit) {
            this.downloadRateLimit = downloadRateLimit;
        }

        public String getUploadRateLimit() {
            return uploadRateLimit;
        }

        public void setUploadRateLimit(String uploadRateLimit) {
            this.uploadRateLimit = uploadRateLimit;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }

        public int getMaxUploads() {
            return maxUploads;
        }

        public void setMaxUploads(int maxUploads) {
            this.maxUploads = maxUploads;
        }
    }

    /**
     * Storage configuration.
     */
    public static class StorageConfig {
        private String downloadPath = "./data/torrents";
        private String tempPath = "./data/torrents/temp";
        private boolean resumeData = true;

        public String getDownloadPath() {
            return downloadPath;
        }

        public void setDownloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
        }

        public String getTempPath() {
            return tempPath;
        }

        public void setTempPath(String tempPath) {
            this.tempPath = tempPath;
        }

        public boolean isResumeData() {
            return resumeData;
        }

        public void setResumeData(boolean resumeData) {
            this.resumeData = resumeData;
        }
    }

    /**
     * DHT configuration.
     */
    public static class DhtConfig {
        private boolean enabled = true;
        private int port = 6881;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    /**
     * Tracker configuration.
     */
    public static class TrackerConfig {
        private boolean enabled = true;
        private List<String> trackers = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getTrackers() {
            return trackers;
        }

        public void setTrackers(List<String> trackers) {
            this.trackers = trackers;
        }
    }

    /**
     * Seeding configuration.
     */
    public static class SeedingConfig {
        private boolean enabled = true;
        private double ratioLimit = 2.0;
        private Duration seedingTimeLimit = Duration.ofHours(168);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public double getRatioLimit() {
            return ratioLimit;
        }

        public void setRatioLimit(double ratioLimit) {
            this.ratioLimit = ratioLimit;
        }

        public Duration getSeedingTimeLimit() {
            return seedingTimeLimit;
        }

        public void setSeedingTimeLimit(Duration seedingTimeLimit) {
            this.seedingTimeLimit = seedingTimeLimit;
        }
    }

    /**
     * Federation configuration.
     */
    public static class FederationConfig {
        private boolean shareTorrents = true;
        private List<String> trustedTrackers = new ArrayList<>();

        public boolean isShareTorrents() {
            return shareTorrents;
        }

        public void setShareTorrents(boolean shareTorrents) {
            this.shareTorrents = shareTorrents;
        }

        public List<String> getTrustedTrackers() {
            return trustedTrackers;
        }

        public void setTrustedTrackers(List<String> trustedTrackers) {
            this.trustedTrackers = trustedTrackers;
        }
    }
}
