package org.joinmastodon.web.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.joinmastodon.core.entity.OAuthAccessToken;
import org.joinmastodon.core.entity.User;
import org.joinmastodon.core.service.OAuthAccessTokenService;
import org.joinmastodon.web.api.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    private final OAuthAccessTokenService accessTokenService;

    public BearerTokenAuthenticationFilter(OAuthAccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String tokenValue = header.substring(7).trim();
            OAuthAccessToken token = accessTokenService.findByToken(tokenValue).orElse(null);
            if (token == null || token.isRevoked() || token.isExpired(Instant.now())) {
                writeUnauthorized(response, "invalid_token");
                return;
            }

            Set<String> scopes = ScopeUtil.parse(token.getScopes());
            List<GrantedAuthority> authorities = scopes.stream()
                    .map(scope -> (GrantedAuthority) new SimpleGrantedAuthority("SCOPE_" + scope))
                    .toList();

            User user = token.getUser();
            Long userId = user != null ? user.getId() : null;
            Long accountId = user != null && user.getAccount() != null ? user.getAccount().getId() : null;
            Long applicationId = token.getApplication() != null ? token.getApplication().getId() : null;
            User.Role role = user != null ? user.getRole() : User.Role.USER;
            AuthenticatedPrincipal principal = new AuthenticatedPrincipal(userId, accountId, applicationId, scopes, role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, tokenValue, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse error = ErrorResponse.of(message, "Unauthorized");
        response.getWriter().write("{\"error\":\"" + error.error() + "\",\"error_description\":\""
                + error.errorDescription() + "\"}");
    }
}
