package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.OAuthAuthorizationCode;
import org.joinmastodon.core.repository.OAuthAuthorizationCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuthAuthorizationCodeService {
    private final OAuthAuthorizationCodeRepository authorizationCodeRepository;

    public OAuthAuthorizationCodeService(OAuthAuthorizationCodeRepository authorizationCodeRepository) {
        this.authorizationCodeRepository = authorizationCodeRepository;
    }

    @Transactional(readOnly = true)
    public Optional<OAuthAuthorizationCode> findByCode(String code) {
        return authorizationCodeRepository.findByCode(code);
    }

    @Transactional
    public OAuthAuthorizationCode save(OAuthAuthorizationCode code) {
        return authorizationCodeRepository.save(code);
    }
}
