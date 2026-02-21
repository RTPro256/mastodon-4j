package org.joinmastodon.torrent.integration;

import org.joinmastodon.torrent.config.TorrentProperties;
import org.joinmastodon.torrent.entity.AccountTorrentSettings;
import org.joinmastodon.torrent.entity.SeedingStatus;
import org.joinmastodon.torrent.entity.SharedTorrent;
import org.joinmastodon.torrent.magnet.MagnetLinkBuilder;
import org.joinmastodon.torrent.repository.AccountTorrentSettingsRepository;
import org.joinmastodon.torrent.repository.SharedTorrentRepository;
import org.joinmastodon.torrent.torrentfile.TorrentFileBuilder;
import org.joinmastodon.torrent.torrentfile.TorrentFileInfo;
import org.joinmastodon.torrent.tracker.PublicTrackerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Bridge service for integrating torrent functionality with media attachments.
 * 
 * This service provides the connection between the mastodon-media module
 * and the mastodon-torrent module, enabling media files to be shared via
 * BitTorrent.
 */
@Service
public class MediaTorrentBridge {

    private static final Logger log = LoggerFactory.getLogger(MediaTorrentBridge.class);

    private final TorrentProperties properties;
    private final SharedTorrentRepository torrentRepository;
    private final AccountTorrentSettingsRepository settingsRepository;
    private final TorrentFileBuilder torrentFileBuilder;
    private final MagnetLinkBuilder magnetLinkBuilder;

    public MediaTorrentBridge(
            TorrentProperties properties,
            SharedTorrentRepository torrentRepository,
            AccountTorrentSettingsRepository settingsRepository) {
        this.properties = properties;
        this.torrentRepository = torrentRepository;
        this.settingsRepository = settingsRepository;
        this.torrentFileBuilder = new TorrentFileBuilder();
        this.magnetLinkBuilder = new MagnetLinkBuilder();
    }

    /**
     * Create a torrent for a media attachment.
     *
     * @param mediaFilePath    Path to the media file on disk
     * @param fileName         Display name for the torrent
     * @param mediaAttachmentId Media attachment ID
     * @param accountId        Account ID that owns the media
     * @param additionalTrackers Additional tracker URLs
     * @return The created SharedTorrent entity
     * @throws IOException If file operations fail
     */
    @Transactional
    public SharedTorrent createTorrentForMedia(
            String mediaFilePath,
            String fileName,
            Long mediaAttachmentId,
            Long accountId,
            List<String> additionalTrackers) throws IOException {

        Path filePath = Paths.get(mediaFilePath);
        if (!Files.exists(filePath)) {
            throw new IOException("Media file does not exist: " + mediaFilePath);
        }

        // Check if torrent already exists for this media attachment
        Optional<SharedTorrent> existing = torrentRepository.findByMediaAttachmentId(mediaAttachmentId);
        if (existing.isPresent()) {
            log.info("Torrent already exists for media attachment: {}", mediaAttachmentId);
            return existing.get();
        }

        // Check if user has torrent sharing enabled
        boolean sharingEnabled = settingsRepository.findByAccountId(accountId)
                .map(AccountTorrentSettings::isShareViaTorrent)
                .orElse(false);

        if (!sharingEnabled) {
            throw new IllegalStateException("Torrent sharing is not enabled for account: " + accountId);
        }

        // Get trackers
        List<String> trackers = buildTrackerList(additionalTrackers);

        // Build torrent file
        TorrentFileInfo torrentInfo = torrentFileBuilder
                .name(fileName)
                .file(filePath)
                .trackers(trackers)
                .createdBy("mastodon-torrent")
                .comment("Shared via Mastodon")
                .build();

        log.info("Created torrent for media: infoHash={}, name={}, size={}", 
                torrentInfo.getInfoHash(), torrentInfo.getName(), torrentInfo.getTotalSize());

        // Save .torrent file
        Path torrentFilePath = saveTorrentFile(torrentInfo);

        // Generate magnet link
        String magnetLink = magnetLinkBuilder.buildUri(torrentInfo);

        // Create database entity
        SharedTorrent sharedTorrent = new SharedTorrent();
        sharedTorrent.setAccountId(accountId);
        sharedTorrent.setInfoHash(torrentInfo.getInfoHash());
        sharedTorrent.setTorrentName(torrentInfo.getName());
        sharedTorrent.setTotalSize(torrentInfo.getTotalSize());
        sharedTorrent.setPieceSize(torrentInfo.getPieceSize());
        sharedTorrent.setPieceCount(torrentInfo.getPieceCount());
        sharedTorrent.setTorrentFilePath(torrentFilePath.toString());
        sharedTorrent.setMagnetLink(magnetLink);
        sharedTorrent.setTrackers(trackers);
        sharedTorrent.setSeedingEnabled(true);
        sharedTorrent.setSeedingStatus(SeedingStatus.INITIALIZING);
        sharedTorrent.setCreatedAt(Instant.now());

        return torrentRepository.save(sharedTorrent);
    }

    /**
     * Create a torrent with default trackers.
     */
    public SharedTorrent createTorrentForMedia(
            String mediaFilePath,
            String fileName,
            Long mediaAttachmentId,
            Long accountId) throws IOException {
        return createTorrentForMedia(mediaFilePath, fileName, mediaAttachmentId, accountId, null);
    }

    /**
     * Start seeding a torrent.
     *
     * @param torrent The torrent to start seeding
     */
    public void startSeeding(SharedTorrent torrent) {
        if (torrent.getTorrentFilePath() == null) {
            throw new IllegalStateException("Torrent file path is not set");
        }

        // In production, this would interact with the actual torrent client
        // For now, we just update the status
        torrent.setSeedingStatus(SeedingStatus.ACTIVE);
        torrentRepository.save(torrent);

        log.info("Started seeding torrent: {}", torrent.getInfoHash());
    }

    /**
     * Stop seeding a torrent.
     *
     * @param torrent The torrent to stop seeding
     */
    public void stopSeeding(SharedTorrent torrent) {
        torrent.setSeedingStatus(SeedingStatus.STOPPED);
        torrentRepository.save(torrent);

        log.info("Stopped seeding torrent: {}", torrent.getInfoHash());
    }

    /**
     * Pause seeding a torrent.
     *
     * @param torrent The torrent to pause
     */
    public void pauseSeeding(SharedTorrent torrent) {
        torrent.setSeedingStatus(SeedingStatus.PAUSED);
        torrentRepository.save(torrent);

        log.info("Paused seeding torrent: {}", torrent.getInfoHash());
    }

    /**
     * Get the torrent for a media attachment.
     *
     * @param mediaAttachmentId The media attachment ID
     * @return The SharedTorrent if it exists
     */
    public Optional<SharedTorrent> getTorrentForMedia(Long mediaAttachmentId) {
        return torrentRepository.findByMediaAttachmentId(mediaAttachmentId);
    }

    /**
     * Check if a torrent exists for a media attachment.
     *
     * @param mediaAttachmentId The media attachment ID
     * @return true if a torrent exists
     */
    public boolean hasTorrent(Long mediaAttachmentId) {
        return torrentRepository.existsByMediaAttachmentId(mediaAttachmentId);
    }

    /**
     * Delete the torrent for a media attachment.
     *
     * @param mediaAttachmentId The media attachment ID
     * @param deleteFiles Whether to delete the .torrent file
     */
    @Transactional
    public void deleteTorrentForMedia(Long mediaAttachmentId, boolean deleteFiles) {
        torrentRepository.findByMediaAttachmentId(mediaAttachmentId).ifPresent(torrent -> {
            if (deleteFiles && torrent.getTorrentFilePath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(torrent.getTorrentFilePath()));
                } catch (IOException e) {
                    log.warn("Failed to delete torrent file: {}", torrent.getTorrentFilePath(), e);
                }
            }
            torrentRepository.delete(torrent);
            log.info("Deleted torrent for media attachment: {}", mediaAttachmentId);
        });
    }

    /**
     * Build the tracker list from configuration and additional trackers.
     */
    private List<String> buildTrackerList(List<String> additionalTrackers) {
        List<String> trackers = new ArrayList<>();

        // Add default trackers from configuration
        if (properties.getTracker().isEnabled()) {
            trackers.addAll(PublicTrackerList.getRecommendedTrackers());
        }

        // Add trusted trackers from federation config
        if (properties.getFederation().isShareTorrents()) {
            trackers.addAll(properties.getFederation().getTrustedTrackers());
        }

        // Add additional trackers
        if (additionalTrackers != null && !additionalTrackers.isEmpty()) {
            trackers.addAll(additionalTrackers);
        }

        return trackers;
    }

    /**
     * Save the .torrent file to disk.
     */
    private Path saveTorrentFile(TorrentFileInfo torrentInfo) throws IOException {
        Path torrentDir = Paths.get(properties.getStorage().getDownloadPath(), "torrents");
        Files.createDirectories(torrentDir);

        Path torrentFilePath = torrentDir.resolve(torrentInfo.getInfoHash() + ".torrent");
        byte[] torrentBytes = torrentFileBuilder.buildTorrentBytes(torrentInfo);
        Files.write(torrentFilePath, torrentBytes);

        log.debug("Saved torrent file to: {}", torrentFilePath);
        return torrentFilePath;
    }
}
