package org.joinmastodon.web.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.entity.OAuthAccessToken;
import org.joinmastodon.core.entity.OAuthRefreshToken;
import org.joinmastodon.core.entity.User;
import org.joinmastodon.core.service.ApplicationService;
import org.joinmastodon.core.service.OAuthAccessTokenService;
import org.joinmastodon.core.service.OAuthRefreshTokenService;
import org.joinmastodon.core.service.PasswordService;
import org.joinmastodon.core.service.UserService;
import org.joinmastodon.web.api.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class OAuthService {
    private final ApplicationService applicationService;
    private final UserService userService;
    private final PasswordService passwordService;
    private final OAuthAccessTokenService accessTokenService;
    private final OAuthRefreshTokenService refreshTokenService;
    private final TokenGenerator tokenGenerator;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public OAuthService(
            ApplicationService applicationService,
            UserService userService,
            PasswordService passwordService,
            OAuthAccessTokenService accessTokenService,
            OAuthRefreshTokenService refreshTokenService,
            TokenGenerator tokenGenerator,
            @Value("${mastodon.auth.access-token-ttl-seconds:7200}") long accessTokenTtlSeconds,
            @Value("${mastodon.auth.refresh-token-ttl-seconds:2592000}") long refreshTokenTtlSeconds) {
        this.applicationService = applicationService;
        this.userService = userService;
        this.passwordService = passwordService;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.tokenGenerator = tokenGenerator;
        this.accessTokenTtl = Duration.ofSeconds(accessTokenTtlSeconds);
        this.refreshTokenTtl = Duration.ofSeconds(refreshTokenTtlSeconds);
    }

    public TokenResponse issueToken(
            String grantType,
            String clientId,
            String clientSecret,
            String username,
            String password,
            String scope,
            String refreshToken) {
        Application application = authenticateClient(clientId, clientSecret);

        if ("password".equals(grantType)) {
            User user = authenticateUser(username, password);
            String resolvedScope = resolveScopes(scope, application.getScopes());
            OAuthAccessToken accessToken = createAccessToken(application, user, resolvedScope, accessTokenTtl);
            OAuthRefreshToken refresh = createRefreshToken(application, user, accessToken, resolvedScope, refreshTokenTtl);
            return tokenResponse(accessToken, refresh);
        }

        if ("client_credentials".equals(grantType)) {
            String resolvedScope = resolveScopes(scope, application.getScopes());
            OAuthAccessToken accessToken = createAccessToken(application, null, resolvedScope, accessTokenTtl);
            return tokenResponse(accessToken, null);
        }

        if ("refresh_token".equals(grantType)) {
            return refreshAccessToken(application, refreshToken, scope);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported_grant_type");
    }

    public void revokeToken(String token) {
        Optional<OAuthAccessToken> accessToken = accessTokenService.findByToken(token);
        if (accessToken.isPresent()) {
            OAuthAccessToken existing = accessToken.get();
            existing.setRevokedAt(Instant.now());
            accessTokenService.save(existing);
            return;
        }

        Optional<OAuthRefreshToken> refreshToken = refreshTokenService.findByToken(token);
        if (refreshToken.isPresent()) {
            OAuthRefreshToken existing = refreshToken.get();
            existing.setRevokedAt(Instant.now());
            refreshTokenService.save(existing);
            OAuthAccessToken linked = existing.getAccessToken();
            if (linked != null) {
                linked.setRevokedAt(Instant.now());
                accessTokenService.save(linked);
            }
        }
    }

    private Application authenticateClient(String clientId, String clientSecret) {
        if (clientId == null || clientSecret == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_client");
        }
        return applicationService.findByClientId(clientId)
                .filter(app -> clientSecret.equals(app.getClientSecret()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_client"));
    }

    private User authenticateUser(String username, String password) {
        if (username == null || password == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_grant");
        }
        User user = userService.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_grant"));
        if (!passwordService.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_grant");
        }
        return user;
    }

    private TokenResponse refreshAccessToken(Application application, String refreshTokenValue, String requestedScope) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_grant");
        }
        OAuthRefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_grant"));
        if (refreshToken.isRevoked() || refreshToken.isExpired(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_grant");
        }
        if (!refreshToken.getApplication().getId().equals(application.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_grant");
        }
        String resolvedScope = resolveScopes(requestedScope, refreshToken.getScopes());
        OAuthAccessToken accessToken = createAccessToken(application, refreshToken.getUser(), resolvedScope, accessTokenTtl);
        return tokenResponse(accessToken, refreshToken);
    }

    private String resolveScopes(String requested, String allowed) {
        try {
            return ScopeUtil.resolveRequestedScopes(requested, allowed, "read");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private OAuthAccessToken createAccessToken(
            Application application,
            User user,
            String scope,
            Duration ttl) {
        OAuthAccessToken accessToken = new OAuthAccessToken();
        accessToken.setApplication(application);
        accessToken.setUser(user);
        accessToken.setToken(tokenGenerator.generate());
        accessToken.setScopes(scope);
        accessToken.setExpiresAt(Instant.now().plus(ttl));
        return accessTokenService.save(accessToken);
    }

    private OAuthRefreshToken createRefreshToken(
            Application application,
            User user,
            OAuthAccessToken accessToken,
            String scope,
            Duration ttl) {
        OAuthRefreshToken refreshToken = new OAuthRefreshToken();
        refreshToken.setApplication(application);
        refreshToken.setUser(user);
        refreshToken.setAccessToken(accessToken);
        refreshToken.setToken(tokenGenerator.generate());
        refreshToken.setScopes(scope);
        refreshToken.setExpiresAt(Instant.now().plus(ttl));
        return refreshTokenService.save(refreshToken);
    }

    private TokenResponse tokenResponse(OAuthAccessToken accessToken, OAuthRefreshToken refreshToken) {
        long createdAt = accessToken.getCreatedAt() != null
                ? accessToken.getCreatedAt().getEpochSecond()
                : Instant.now().getEpochSecond();
        Long expiresIn = accessToken.getExpiresAt() != null
                ? Duration.between(Instant.now(), accessToken.getExpiresAt()).getSeconds()
                : null;
        String refresh = refreshToken != null ? refreshToken.getToken() : null;
        return new TokenResponse(accessToken.getToken(), "Bearer", accessToken.getScopes(), createdAt, expiresIn, refresh);
    }
}
