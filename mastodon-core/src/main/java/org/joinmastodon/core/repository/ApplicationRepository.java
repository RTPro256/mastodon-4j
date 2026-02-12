package org.joinmastodon.core.repository;

import java.util.Optional;
import org.joinmastodon.core.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByClientId(String clientId);
}
