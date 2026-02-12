package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
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

    @Transactional
    public Status save(Status status) {
        return statusRepository.save(status);
    }
}
