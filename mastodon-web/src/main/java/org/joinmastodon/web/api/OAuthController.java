package org.joinmastodon.web.api;

import jakarta.validation.constraints.NotBlank;
import org.joinmastodon.web.api.dto.TokenResponse;
import org.joinmastodon.web.auth.OAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping
public class OAuthController {
    private final OAuthService oauthService;

    public OAuthController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @PostMapping("/oauth/token")
    public TokenResponse token(
            @RequestParam("grant_type") @NotBlank String grantType,
            @RequestParam("client_id") @NotBlank String clientId,
            @RequestParam("client_secret") @NotBlank String clientSecret,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "refresh_token", required = false) String refreshToken) {
        return oauthService.issueToken(grantType, clientId, clientSecret, username, password, scope, refreshToken);
    }

    @PostMapping("/oauth/revoke")
    public ResponseEntity<Void> revoke(@RequestParam("token") @NotBlank String token) {
        oauthService.revokeToken(token);
        return ResponseEntity.ok().build();
    }
}
