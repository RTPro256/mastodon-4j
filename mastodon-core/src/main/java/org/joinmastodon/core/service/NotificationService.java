package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Notification;
import org.joinmastodon.core.repository.NotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByAccountWithCursor(Account account, Long maxId, Long sinceId, Pageable pageable) {
        return notificationRepository.findByAccountWithCursor(account, maxId, sinceId, pageable);
    }
}
