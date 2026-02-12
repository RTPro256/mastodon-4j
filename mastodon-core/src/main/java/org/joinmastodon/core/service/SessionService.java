package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Session;
import org.joinmastodon.core.entity.User;
import org.joinmastodon.core.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Session> findById(String sessionId) {
        return sessionRepository.findById(sessionId);
    }

    @Transactional(readOnly = true)
    public List<Session> findByUser(User user) {
        return sessionRepository.findByUser(user);
    }

    @Transactional
    public Session save(Session session) {
        return sessionRepository.save(session);
    }

    @Transactional
    public void delete(Session session) {
        sessionRepository.delete(session);
    }
}
