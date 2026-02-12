package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.OAuthRefreshToken;
import org.joinmastodon.core.repository.OAuthRefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuthRefreshTokenService {
    private final OAuthRefreshTokenRepository refreshTokenRepository;

    public OAuthRefreshTokenService(OAuthRefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(readOnly = true)
    public Optional<OAuthRefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public OAuthRefreshToken save(OAuthRefreshToken token) {
        return refreshTokenRepository.save(token);
    }
}
