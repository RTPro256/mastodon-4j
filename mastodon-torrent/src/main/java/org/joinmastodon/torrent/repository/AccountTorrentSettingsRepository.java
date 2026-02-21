package org.joinmastodon.torrent.repository;

import org.joinmastodon.torrent.entity.AccountTorrentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for AccountTorrentSettings entities.
 */
@Repository
public interface AccountTorrentSettingsRepository extends JpaRepository<AccountTorrentSettings, Long> {

    /**
     * Find settings by account ID.
     */
    Optional<AccountTorrentSettings> findByAccountId(Long accountId);

    /**
     * Check if settings exist for an account.
     */
    boolean existsByAccountId(Long accountId);

    /**
     * Delete settings for an account.
     */
    void deleteByAccountId(Long accountId);
}
