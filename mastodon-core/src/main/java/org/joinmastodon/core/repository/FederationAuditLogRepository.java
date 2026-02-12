package org.joinmastodon.core.repository;

import org.joinmastodon.core.entity.FederationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FederationAuditLogRepository extends JpaRepository<FederationAuditLog, Long> {
}
