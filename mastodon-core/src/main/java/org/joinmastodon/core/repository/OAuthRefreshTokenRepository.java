package org.joinmastodon.core.repository;

import java.util.Optional;
import org.joinmastodon.core.entity.OAuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthRefreshTokenRepository extends JpaRepository<OAuthRefreshToken, Long> {
    Optional<OAuthRefreshToken> findByToken(String token);
}
