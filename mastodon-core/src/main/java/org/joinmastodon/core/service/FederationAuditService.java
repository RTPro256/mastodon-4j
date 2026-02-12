package org.joinmastodon.core.service;

import org.joinmastodon.core.entity.FederationAuditLog;
import org.joinmastodon.core.repository.FederationAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FederationAuditService {
    private final FederationAuditLogRepository repository;

    public FederationAuditService(FederationAuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FederationAuditLog recordInbound(String eventType, String actorUri, String targetUri, String status, String error) {
        return save("INBOUND", eventType, actorUri, targetUri, status, error);
    }

    @Transactional
    public FederationAuditLog recordOutbound(String eventType, String actorUri, String targetUri, String status, String error) {
        return save("OUTBOUND", eventType, actorUri, targetUri, status, error);
    }

    private FederationAuditLog save(String direction, String eventType, String actorUri, String targetUri, String status, String error) {
        FederationAuditLog log = new FederationAuditLog();
        log.setDirection(direction);
        log.setEventType(eventType);
        log.setActorUri(actorUri);
        log.setTargetUri(targetUri);
        log.setStatus(status);
        log.setError(error);
        return repository.save(log);
    }
}
