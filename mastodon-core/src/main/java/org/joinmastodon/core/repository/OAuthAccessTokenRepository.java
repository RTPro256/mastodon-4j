package org.joinmastodon.core.repository;

import java.util.Optional;
import org.joinmastodon.core.entity.OAuthAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthAccessTokenRepository extends JpaRepository<OAuthAccessToken, Long> {
    Optional<OAuthAccessToken> findByToken(String token);
}
