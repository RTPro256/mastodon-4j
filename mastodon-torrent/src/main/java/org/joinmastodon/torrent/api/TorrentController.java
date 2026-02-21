package org.joinmastodon.torrent.api;

import org.joinmastodon.torrent.api.dto.CreateTorrentRequest;
import org.joinmastodon.torrent.api.dto.TorrentResponse;
import org.joinmastodon.torrent.entity.AccountTorrentSettings;
import org.joinmastodon.torrent.entity.SeedingStatus;
import org.joinmastodon.torrent.entity.SharedTorrent;
import org.joinmastodon.torrent.service.TorrentCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API controller for torrent operations.
 */
@RestController
@RequestMapping("/api/v2/torrents")
public class TorrentController {

    private static final Logger log = LoggerFactory.getLogger(TorrentController.class);

    private final TorrentCreationService torrentService;

    public TorrentController(TorrentCreationService torrentService) {
        this.torrentService = torrentService;
    }

    /**
     * List all torrents for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<TorrentResponse>> listTorrents(
            @RequestHeader("accountId") Long accountId) {
        List<SharedTorrent> torrents = torrentService.getAccountTorrents(accountId);
        List<TorrentResponse> responses = torrents.stream()
                .map(t -> TorrentResponse.from(t, ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Create a new torrent from a media attachment.
     */
    @PostMapping
    public ResponseEntity<?> createTorrent(
            @RequestHeader("accountId") Long accountId,
            @RequestBody CreateTorrentRequest request) {
        
        // Check if torrent sharing is enabled
        if (!torrentService.isTorrentSharingEnabled(accountId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Torrent sharing is not enabled for this account"));
        }

        try {
            // Parse media attachment ID
            Long mediaAttachmentId = parseId(request.getMediaAttachmentId());
            
            // In production, you would:
            // 1. Fetch the MediaAttachment entity
            // 2. Get the file path from the storage service
            // 3. Create the torrent
            
            // For now, return a placeholder response
            return ResponseEntity.ok(Map.of(
                    "message", "Torrent creation requires integration with media storage",
                    "mediaAttachmentId", mediaAttachmentId
            ));
            
        } catch (Exception e) {
            log.error("Failed to create torrent", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create torrent: " + e.getMessage()));
        }
    }

    /**
     * Get torrent details.
     */
    @GetMapping("/{infoHash}")
    public ResponseEntity<?> getTorrent(
            @PathVariable String infoHash,
            @RequestHeader("accountId") Long accountId) {
        
        SharedTorrent torrent = torrentService.getTorrent(infoHash);
        if (torrent == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verify ownership
        if (!torrent.getAccountId().equals(accountId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(TorrentResponse.from(torrent, ""));
    }

    /**
     * Delete a torrent.
     */
    @DeleteMapping("/{infoHash}")
    public ResponseEntity<?> deleteTorrent(
            @PathVariable String infoHash,
            @RequestHeader("accountId") Long accountId,
            @RequestParam(defaultValue = "false") boolean deleteFiles) {
        
        SharedTorrent torrent = torrentService.getTorrent(infoHash);
        if (torrent == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verify ownership
        if (!torrent.getAccountId().equals(accountId)) {
            return ResponseEntity.notFound().build();
        }
        
        torrentService.deleteTorrent(infoHash, deleteFiles);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the magnet link for a torrent.
     */
    @GetMapping("/{infoHash}/magnet")
    public ResponseEntity<?> getMagnetLink(
            @PathVariable String infoHash,
            @RequestHeader("accountId") Long accountId) {
        
        SharedTorrent torrent = torrentService.getTorrent(infoHash);
        if (torrent == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verify ownership
        if (!torrent.getAccountId().equals(accountId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of("magnetLink", torrent.getMagnetLink()));
    }

    /**
     * Download the .torrent file.
     */
    @GetMapping("/{infoHash}/torrent")
    public ResponseEntity<Resource> downloadTorrentFile(
            @PathVariable String infoHash,
            @RequestHeader("accountId") Long accountId) {
        
        SharedTorrent torrent = torrentService.getTorrent(infoHash);
        if (torrent == null || torrent.getTorrentFilePath() == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verify ownership
        if (!torrent.getAccountId().equals(accountId)) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            Path filePath = Paths.get(torrent.getTorrentFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            String filename = torrent.getTorrentName() + ".torrent";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (MalformedURLException e) {
            log.error("Failed to load torrent file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Pause seeding a torrent.
     */
    @PostMapping("/{infoHash}/pause")
    public ResponseEntity<?> pauseTorrent(
            @PathVariable String infoHash,
            @RequestHeader("accountId") Long accountId) {
        
        SharedTorrent torrent = torrentService.getTorrent(infoHash);
        if (torrent == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verify ownership
        if (!torrent.getAccountId().equals(accountId)) {
            return ResponseEntity.notFound().build();
        }
        
        // In production, this would interact with the torrent client
        torrent.setSeedingStatus(SeedingStatus.PAUSED);
        
        return ResponseEntity.ok(TorrentResponse.from(torrent, ""));
    }

    /**
     * Resume seeding a torrent.
     */
    @PostMapping("/{infoHash}/resume")
    public ResponseEntity<?> resumeTorrent(
            @PathVariable String infoHash,
            @RequestHeader("accountId") Long accountId) {
        
        SharedTorrent torrent = torrentService.getTorrent(infoHash);
        if (torrent == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verify ownership
        if (!torrent.getAccountId().equals(accountId)) {
            return ResponseEntity.notFound().build();
        }
        
        // In production, this would interact with the torrent client
        torrent.setSeedingStatus(SeedingStatus.ACTIVE);
        
        return ResponseEntity.ok(TorrentResponse.from(torrent, ""));
    }

    /**
     * Get user's torrent settings.
     */
    @GetMapping("/settings")
    public ResponseEntity<AccountTorrentSettings> getSettings(
            @RequestHeader("accountId") Long accountId) {
        AccountTorrentSettings settings = torrentService.getOrCreateSettings(accountId);
        return ResponseEntity.ok(settings);
    }

    /**
     * Update user's torrent settings.
     */
    @PutMapping("/settings")
    public ResponseEntity<AccountTorrentSettings> updateSettings(
            @RequestHeader("accountId") Long accountId,
            @RequestBody Map<String, Object> settings) {
        
        boolean shareViaTorrent = (boolean) settings.getOrDefault("shareViaTorrent", false);
        boolean autoSeedUploads = (boolean) settings.getOrDefault("autoSeedUploads", true);
        Double maxSeedingRatio = settings.get("maxSeedingRatio") != null 
                ? ((Number) settings.get("maxSeedingRatio")).doubleValue() 
                : 2.0;
        Integer maxSeedingHours = settings.get("maxSeedingHours") != null 
                ? ((Number) settings.get("maxSeedingHours")).intValue() 
                : 168;
        String maxUploadRate = (String) settings.getOrDefault("maxUploadRate", "1MB");
        boolean anonymousSeeding = (boolean) settings.getOrDefault("anonymousSeeding", false);
        
        AccountTorrentSettings updated = torrentService.updateSettings(
                accountId, shareViaTorrent, autoSeedUploads, maxSeedingRatio,
                maxSeedingHours, maxUploadRate, anonymousSeeding);
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Parse a string ID to Long.
     */
    private Long parseId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format: " + id);
        }
    }
}
