package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.ListEntity;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.model.Visibility;
import org.joinmastodon.core.repository.StatusRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusService {
    private final StatusRepository statusRepository;

    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Status> findById(Long id) {
        return statusRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Status> findByAccount(Account account, Pageable pageable) {
        return statusRepository.findByAccountOrderByCreatedAtDesc(account, pageable);
    }

    @Transactional(readOnly = true)
    public List<Status> findByAccountWithCursor(Account account, Long maxId, Long sinceId, Pageable pageable) {
        return statusRepository.findByAccountWithCursor(account, maxId, sinceId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Status> findPublicTimeline(List<Visibility> visibilities, Long maxId, Long sinceId, Pageable pageable) {
        return statusRepository.findPublicTimeline(visibilities, maxId, sinceId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Status> findHomeTimeline(Account account, Long maxId, Long sinceId, Pageable pageable) {
        return statusRepository.findHomeTimeline(account, maxId, sinceId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Status> findByTag(String tag, Long maxId, Long sinceId, Pageable pageable) {
        return statusRepository.findByTag(tag, maxId, sinceId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Status> findByList(ListEntity list, Long maxId, Long sinceId, Pageable pageable) {
        return statusRepository.findByList(list, maxId, sinceId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Status> searchByContent(String query, Pageable pageable) {
        return statusRepository.findByContentContainingIgnoreCase(query, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Status> findByAccountAndReblog(Account account, Status reblog) {
        return statusRepository.findByAccountAndReblog(account, reblog);
    }

    @Transactional(readOnly = true)
    public List<Status> findReplies(Long statusId) {
        return statusRepository.findByInReplyToIdOrderByIdAsc(statusId);
    }

    @Transactional
    public Status save(Status status) {
        return statusRepository.save(status);
    }

    @Transactional
    public void delete(Status status) {
        statusRepository.delete(status);
    }
}
