package org.joinmastodon.web.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.joinmastodon.core.service.RateLimitService;
import org.joinmastodon.web.api.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimitService;
    private final int maxRequests;
    private final Duration window;

    public RateLimitFilter(RateLimitService rateLimitService, int maxRequests, Duration window) {
        this.rateLimitService = rateLimitService;
        this.maxRequests = maxRequests;
        this.window = window;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String key = buildKey(request);
        boolean allowed = rateLimitService.allow(key, Instant.now(), maxRequests, window);
        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = ErrorResponse.of("rate_limit_exceeded", "Too many requests");
            response.getWriter().write("{\"error\":\"" + error.error() + "\",\"error_description\":\""
                    + error.errorDescription() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String buildKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            if (principal.userId() != null) {
                return "user:" + principal.userId();
            }
        }
        String ip = request.getRemoteAddr();
        return "ip:" + (ip == null ? "unknown" : ip);
    }
}
