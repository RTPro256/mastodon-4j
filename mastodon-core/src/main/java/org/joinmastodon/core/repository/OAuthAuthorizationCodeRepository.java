package org.joinmastodon.core.repository;

import java.util.Optional;
import org.joinmastodon.core.entity.OAuthAuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthAuthorizationCodeRepository extends JpaRepository<OAuthAuthorizationCode, Long> {
    Optional<OAuthAuthorizationCode> findByCode(String code);
}
