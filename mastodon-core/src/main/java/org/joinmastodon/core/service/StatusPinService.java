package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Status;
import org.joinmastodon.core.entity.StatusPin;
import org.joinmastodon.core.repository.StatusPinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusPinService {
    private final StatusPinRepository statusPinRepository;

    public StatusPinService(StatusPinRepository statusPinRepository) {
        this.statusPinRepository = statusPinRepository;
    }

    @Transactional(readOnly = true)
    public List<StatusPin> findByAccount(Account account) {
        return statusPinRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    @Transactional(readOnly = true)
    public Optional<StatusPin> findByAccountAndStatus(Account account, Status status) {
        return statusPinRepository.findByAccountAndStatus(account, status);
    }

    @Transactional(readOnly = true)
    public boolean isPinned(Account account, Status status) {
        return statusPinRepository.existsByAccountAndStatus(account, status);
    }

    @Transactional
    public StatusPin pin(Account account, Status status) {
        if (isPinned(account, status)) {
            return statusPinRepository.findByAccountAndStatus(account, status).orElseThrow();
        }
        StatusPin pin = new StatusPin();
        pin.setAccount(account);
        pin.setStatus(status);
        return statusPinRepository.save(pin);
    }

    @Transactional
    public void unpin(Account account, Status status) {
        statusPinRepository.deleteByAccountAndStatus(account, status);
    }
}
