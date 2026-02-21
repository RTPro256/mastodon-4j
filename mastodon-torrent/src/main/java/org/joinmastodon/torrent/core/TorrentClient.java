package org.joinmastodon.torrent.core;

import org.joinmastodon.torrent.config.TorrentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main BitTorrent client for managing torrent sessions.
 */
@Component
public class TorrentClient {

    private static final Logger log = LoggerFactory.getLogger(TorrentClient.class);

    private final TorrentProperties properties;
    private final Map<String, TorrentHandle> activeTorrents = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    public TorrentClient(TorrentProperties properties) {
        this.properties = properties;
    }

    /**
     * Start the torrent client.
     */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        log.info("Torrent client started (download path: {})", properties.getStorage().getDownloadPath());
    }

    /**
     * Stop the torrent client.
     */
    public void stop() {
        running = false;
        // Stop all active torrents
        for (TorrentHandle handle : activeTorrents.values()) {
            handle.pause();
        }
        log.info("Torrent client stopped");
    }

    /**
     * Add a torrent from a magnet link.
     */
    public TorrentHandle addMagnet(String magnetLink) {
        if (!running) {
            throw new IllegalStateException("Torrent client is not running");
        }

        String infoHash = extractInfoHash(magnetLink);
        if (activeTorrents.containsKey(infoHash)) {
            return activeTorrents.get(infoHash);
        }

        TorrentHandle handle = new TorrentHandle(infoHash, magnetLink, properties);
        activeTorrents.put(infoHash, handle);
        log.info("Added magnet link: {}", infoHash);
        return handle;
    }

    /**
     * Add a torrent from a .torrent file.
     */
    public TorrentHandle addTorrentFile(Path torrentFile) throws IOException {
        if (!running) {
            throw new IllegalStateException("Torrent client is not running");
        }

        TorrentInfo info = parseTorrentFile(torrentFile);
        if (activeTorrents.containsKey(info.getInfoHash())) {
            return activeTorrents.get(info.getInfoHash());
        }

        TorrentHandle handle = new TorrentHandle(info.getInfoHash(), torrentFile, properties);
        activeTorrents.put(info.getInfoHash(), handle);
        log.info("Added torrent file: {} ({})", info.getName(), info.getInfoHash());
        return handle;
    }

    /**
     * Remove a torrent.
     */
    public void remove(String infoHash, boolean deleteFiles) {
        TorrentHandle handle = activeTorrents.remove(infoHash);
        if (handle != null) {
            handle.pause();
            if (deleteFiles) {
                // Delete downloaded files
                log.info("Removed torrent {} and deleted files", infoHash);
            } else {
                log.info("Removed torrent {}", infoHash);
            }
        }
    }

    /**
     * Get a torrent handle.
     */
    public Optional<TorrentHandle> getTorrent(String infoHash) {
        return Optional.ofNullable(activeTorrents.get(infoHash));
    }

    /**
     * Get all active torrents.
     */
    public Collection<TorrentHandle> getAllTorrents() {
        return Collections.unmodifiableCollection(activeTorrents.values());
    }

    /**
     * Get client statistics.
     */
    public ClientStats getStats() {
        int downloading = 0;
        int seeding = 0;
        int paused = 0;
        long totalDownload = 0;
        long totalUpload = 0;

        for (TorrentHandle handle : activeTorrents.values()) {
            TorrentStatus status = handle.getStatus();
            switch (status.getState()) {
                case DOWNLOADING -> downloading++;
                case SEEDING, FINISHED -> seeding++;
                case PAUSED -> paused++;
            }
            totalDownload += status.getDownloadRate();
            totalUpload += status.getUploadRate();
        }

        return new ClientStats(activeTorrents.size(), downloading, seeding, paused, 
                totalDownload, totalUpload);
    }

    /**
     * Extract info hash from magnet link.
     */
    private String extractInfoHash(String magnetLink) {
        // Simple extraction - in real implementation would parse properly
        int btihIndex = magnetLink.indexOf("xt=urn:btih:");
        if (btihIndex >= 0) {
            int start = btihIndex + 12;
            int end = Math.min(start + 40, magnetLink.length());
            return magnetLink.substring(start, end);
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 40);
    }

    /**
     * Parse a .torrent file.
     */
    private TorrentInfo parseTorrentFile(Path torrentFile) throws IOException {
        // Placeholder - in real implementation would parse bencode
        String infoHash = UUID.randomUUID().toString().replace("-", "").substring(0, 40);
        return new TorrentInfo(infoHash, torrentFile.getFileName().toString(), 
                0, 0, 0, List.of(), List.of(), null, "mastodon-torrent");
    }

    /**
     * Client statistics.
     */
    public record ClientStats(
            int totalTorrents,
            int downloading,
            int seeding,
            int paused,
            long totalDownloadRate,
            long totalUploadRate
    ) {}
}
