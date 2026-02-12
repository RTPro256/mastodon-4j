package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.OAuthAccessToken;
import org.joinmastodon.core.repository.OAuthAccessTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuthAccessTokenService {
    private final OAuthAccessTokenRepository accessTokenRepository;

    public OAuthAccessTokenService(OAuthAccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Transactional(readOnly = true)
    public Optional<OAuthAccessToken> findByToken(String token) {
        return accessTokenRepository.findByToken(token);
    }

    @Transactional
    public OAuthAccessToken save(OAuthAccessToken token) {
        return accessTokenRepository.save(token);
    }
}
