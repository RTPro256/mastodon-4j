package org.joinmastodon.torrent.repository;

import org.joinmastodon.torrent.entity.SeedingStatus;
import org.joinmastodon.torrent.entity.SharedTorrent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SharedTorrent entities.
 */
@Repository
public interface SharedTorrentRepository extends JpaRepository<SharedTorrent, Long> {

    /**
     * Find a torrent by its infohash.
     */
    Optional<SharedTorrent> findByInfoHash(String infoHash);

    /**
     * Find a torrent by media attachment ID.
     */
    Optional<SharedTorrent> findByMediaAttachmentId(Long mediaAttachmentId);

    /**
     * Find all torrents for a specific account.
     */
    List<SharedTorrent> findByAccountId(Long accountId);

    /**
     * Find all torrents for a specific account with pagination.
     */
    Page<SharedTorrent> findByAccountId(Long accountId, Pageable pageable);

    /**
     * Find all torrents with a specific seeding status.
     */
    List<SharedTorrent> findBySeedingStatus(SeedingStatus status);

    /**
     * Find all torrents with seeding enabled.
     */
    @Query("SELECT st FROM SharedTorrent st WHERE st.seedingEnabled = true")
    List<SharedTorrent> findAllSeedingEnabled();

    /**
     * Find all torrents with seeding enabled and a specific status.
     */
    List<SharedTorrent> findBySeedingEnabledTrueAndSeedingStatus(SeedingStatus status);

    /**
     * Count torrents for an account.
     */
    long countByAccountId(Long accountId);

    /**
     * Check if a torrent exists for a media attachment.
     */
    boolean existsByMediaAttachmentId(Long mediaAttachmentId);

    /**
     * Check if an infohash exists.
     */
    boolean existsByInfoHash(String infoHash);

    /**
     * Get total uploaded bytes for an account.
     */
    @Query("SELECT COALESCE(SUM(st.totalUploaded), 0) FROM SharedTorrent st WHERE st.accountId = :accountId")
    Long getTotalUploadedByAccountId(@Param("accountId") Long accountId);

    /**
     * Get total downloaded bytes for an account.
     */
    @Query("SELECT COALESCE(SUM(st.totalDownloaded), 0) FROM SharedTorrent st WHERE st.accountId = :accountId")
    Long getTotalDownloadedByAccountId(@Param("accountId") Long accountId);

    /**
     * Find torrents created after a specific time.
     */
    List<SharedTorrent> findByCreatedAtAfter(java.time.Instant createdAt);

    /**
     * Find torrents that need announce (last announce was more than X seconds ago).
     */
    @Query("SELECT st FROM SharedTorrent st WHERE st.seedingEnabled = true " +
           "AND st.seedingStatus = 'ACTIVE' " +
           "AND (st.lastAnnounceAt IS NULL OR st.lastAnnounceAt < :threshold)")
    List<SharedTorrent> findTorrentsNeedingAnnounce(@Param("threshold") java.time.Instant threshold);

    /**
     * Delete all torrents for an account.
     */
    void deleteByAccountId(Long accountId);
}
