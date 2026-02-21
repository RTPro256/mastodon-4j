package org.joinmastodon.core.service;

import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.repository.FavouriteRepository;
import org.joinmastodon.core.repository.StatusPinRepository;
import org.joinmastodon.core.repository.StatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for retrieving status statistics (favourites, reblogs, replies counts).
 */
@Service
public class StatusStatsService {
    private final FavouriteRepository favouriteRepository;
    private final StatusRepository statusRepository;
    private final StatusPinRepository statusPinRepository;

    public StatusStatsService(FavouriteRepository favouriteRepository, StatusRepository statusRepository,
                              StatusPinRepository statusPinRepository) {
        this.favouriteRepository = favouriteRepository;
        this.statusRepository = statusRepository;
        this.statusPinRepository = statusPinRepository;
    }

    @Transactional(readOnly = true)
    public long countFavourites(Long statusId) {
        return favouriteRepository.countByStatusId(statusId);
    }

    @Transactional(readOnly = true)
    public long countReblogs(Long statusId) {
        return statusRepository.countByReblogId(statusId);
    }

    @Transactional(readOnly = true)
    public long countReplies(Long statusId) {
        return statusRepository.countByInReplyToId(statusId);
    }

    @Transactional(readOnly = true)
    public boolean isPinned(Account account, Status status) {
        return statusPinRepository.existsByAccountAndStatus(account, status);
    }

    /**
     * Record containing all stats for a status.
     */
    public record StatusStats(long favouritesCount, long reblogsCount, long repliesCount, boolean pinned) {}

    @Transactional(readOnly = true)
    public StatusStats getStats(Long statusId) {
        return new StatusStats(
            countFavourites(statusId),
            countReblogs(statusId),
            countReplies(statusId),
            false
        );
    }

    @Transactional(readOnly = true)
    public StatusStats getStats(Long statusId, Account account, Status status) {
        return new StatusStats(
            countFavourites(statusId),
            countReblogs(statusId),
            countReplies(statusId),
            isPinned(account, status)
        );
    }
}
