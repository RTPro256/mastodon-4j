package org.joinmastodon.torrent.service;

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

/**
 * Service for creating and managing torrents from media content.
 */
@Service
public class TorrentCreationService {

    private static final Logger log = LoggerFactory.getLogger(TorrentCreationService.class);

    private final TorrentProperties properties;
    private final SharedTorrentRepository torrentRepository;
    private final AccountTorrentSettingsRepository settingsRepository;
    private final TorrentFileBuilder torrentFileBuilder;
    private final MagnetLinkBuilder magnetLinkBuilder;

    public TorrentCreationService(
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
     * Create a torrent from a media file.
     *
     * @param mediaFilePath    Path to the media file
     * @param fileName         Display name for the torrent
     * @param accountId        Account ID that owns the torrent
     * @param additionalTrackers Additional tracker URLs
     * @return The created SharedTorrent entity
     * @throws IOException If file operations fail
     */
    @Transactional
    public SharedTorrent createTorrent(
            String mediaFilePath,
            String fileName,
            Long accountId,
            List<String> additionalTrackers) throws IOException {

        Path filePath = Paths.get(mediaFilePath);
        if (!Files.exists(filePath)) {
            throw new IOException("Media file does not exist: " + mediaFilePath);
        }

        // Check if torrent already exists for this file
        // Note: In production, you'd check by media attachment ID

        // Get trackers
        List<String> trackers = new ArrayList<>();
        if (properties.getTracker().isEnabled()) {
            trackers.addAll(PublicTrackerList.getRecommendedTrackers());
        }
        if (properties.getFederation().isShareTorrents()) {
            trackers.addAll(properties.getFederation().getTrustedTrackers());
        }
        if (additionalTrackers != null) {
            trackers.addAll(additionalTrackers);
        }

        // Build torrent file
        TorrentFileInfo torrentInfo = torrentFileBuilder
                .name(fileName)
                .file(filePath)
                .trackers(trackers)
                .createdBy("mastodon-torrent")
                .build();

        log.info("Created torrent: infoHash={}, name={}, size={}", 
                torrentInfo.getInfoHash(), torrentInfo.getName(), torrentInfo.getTotalSize());

        // Save .torrent file
        Path torrentDir = Paths.get(properties.getStorage().getDownloadPath(), "torrents");
        Files.createDirectories(torrentDir);
        Path torrentFilePath = torrentDir.resolve(torrentInfo.getInfoHash() + ".torrent");
        
        byte[] torrentBytes = torrentFileBuilder.buildTorrentBytes(torrentInfo);
        Files.write(torrentFilePath, torrentBytes);

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
     * Create a torrent from a media file with default trackers.
     */
    public SharedTorrent createTorrent(String mediaFilePath, String fileName, Long accountId) throws IOException {
        return createTorrent(mediaFilePath, fileName, accountId, null);
    }

    /**
     * Check if an account has torrent sharing enabled.
     */
    public boolean isTorrentSharingEnabled(Long accountId) {
        return settingsRepository.findByAccountId(accountId)
                .map(AccountTorrentSettings::isShareViaTorrent)
                .orElse(false);
    }

    /**
     * Get or create default settings for an account.
     */
    @Transactional
    public AccountTorrentSettings getOrCreateSettings(Long accountId) {
        return settingsRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    AccountTorrentSettings settings = new AccountTorrentSettings();
                    settings.setAccountId(accountId);
                    settings.setShareViaTorrent(false);
                    settings.setAutoSeedUploads(true);
                    settings.setMaxSeedingRatio(2.0);
                    settings.setMaxSeedingHours(168);
                    settings.setMaxUploadRate("1MB");
                    settings.setAnonymousSeeding(false);
                    return settingsRepository.save(settings);
                });
    }

    /**
     * Update account torrent settings.
     */
    @Transactional
    public AccountTorrentSettings updateSettings(Long accountId, boolean shareViaTorrent, 
                                                  boolean autoSeedUploads, Double maxSeedingRatio,
                                                  Integer maxSeedingHours, String maxUploadRate,
                                                  boolean anonymousSeeding) {
        AccountTorrentSettings settings = getOrCreateSettings(accountId);
        settings.setShareViaTorrent(shareViaTorrent);
        settings.setAutoSeedUploads(autoSeedUploads);
        if (maxSeedingRatio != null) {
            settings.setMaxSeedingRatio(maxSeedingRatio);
        }
        if (maxSeedingHours != null) {
            settings.setMaxSeedingHours(maxSeedingHours);
        }
        if (maxUploadRate != null) {
            settings.setMaxUploadRate(maxUploadRate);
        }
        settings.setAnonymousSeeding(anonymousSeeding);
        return settingsRepository.save(settings);
    }

    /**
     * Delete a torrent.
     */
    @Transactional
    public void deleteTorrent(String infoHash, boolean deleteFiles) {
        torrentRepository.findByInfoHash(infoHash).ifPresent(torrent -> {
            torrentRepository.delete(torrent);
            
            if (deleteFiles && torrent.getTorrentFilePath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(torrent.getTorrentFilePath()));
                } catch (IOException e) {
                    log.warn("Failed to delete torrent file: {}", torrent.getTorrentFilePath(), e);
                }
            }
            
            log.info("Deleted torrent: {}", infoHash);
        });
    }

    /**
     * Get all torrents for an account.
     */
    public List<SharedTorrent> getAccountTorrents(Long accountId) {
        return torrentRepository.findByAccountId(accountId);
    }

    /**
     * Get a torrent by infohash.
     */
    public SharedTorrent getTorrent(String infoHash) {
        return torrentRepository.findByInfoHash(infoHash).orElse(null);
    }
}
