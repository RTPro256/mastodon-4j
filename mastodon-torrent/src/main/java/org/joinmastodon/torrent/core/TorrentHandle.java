package org.joinmastodon.torrent.core;

import org.joinmastodon.torrent.config.TorrentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Handle for an active torrent.
 */
public class TorrentHandle {

    private static final Logger log = LoggerFactory.getLogger(TorrentHandle.class);

    private final String infoHash;
    private final Object source; // Magnet link or Path
    private final TorrentProperties properties;

    private volatile boolean paused = false;
    private volatile boolean autoManaged = true;
    private volatile TorrentStatus status;

    public TorrentHandle(String infoHash, Object source, TorrentProperties properties) {
        this.infoHash = infoHash;
        this.source = source;
        this.properties = properties;
        this.status = new TorrentStatus(infoHash, TorrentStatus.State.QUEUED, 
                0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Get the info hash.
     */
    public String getInfoHash() {
        return infoHash;
    }

    /**
     * Get current status.
     */
    public TorrentStatus getStatus() {
        return status;
    }

    /**
     * Start/resume the torrent.
     */
    public void resume() {
        if (!paused) {
            return;
        }
        paused = false;
        log.debug("Resumed torrent: {}", infoHash);
    }

    /**
     * Pause the torrent.
     */
    public void pause() {
        if (paused) {
            return;
        }
        paused = true;
        status = new TorrentStatus(infoHash, TorrentStatus.State.PAUSED,
                status.getDownloadedBytes(), status.getUploadedBytes(),
                status.getTotalBytes(), 0, 0, status.getSeeds(), status.getPeers());
        log.debug("Paused torrent: {}", infoHash);
    }

    /**
     * Check if paused.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Set auto-managed mode.
     */
    public void setAutoManaged(boolean autoManaged) {
        this.autoManaged = autoManaged;
    }

    /**
     * Check if auto-managed.
     */
    public boolean isAutoManaged() {
        return autoManaged;
    }

    /**
     * Force recheck the torrent.
     */
    public void forceRecheck() {
        log.debug("Force recheck torrent: {}", infoHash);
        status = new TorrentStatus(infoHash, TorrentStatus.State.CHECKING,
                status.getDownloadedBytes(), status.getUploadedBytes(),
                status.getTotalBytes(), 0, 0, status.getSeeds(), status.getPeers());
    }

    /**
     * Get download progress (0.0 to 1.0).
     */
    public double getProgress() {
        return status.getProgress();
    }

    /**
     * Check if download is complete.
     */
    public boolean isComplete() {
        return status.isComplete();
    }

    /**
     * Update status (called by the client).
     */
    public void updateStatus(TorrentStatus newStatus) {
        this.status = newStatus;
    }
}
