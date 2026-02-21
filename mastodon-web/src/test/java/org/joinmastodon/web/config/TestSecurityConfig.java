package org.joinmastodon.web.config;

import org.joinmastodon.core.repository.UserRepository;
import org.joinmastodon.core.service.OAuthAccessTokenService;
import org.joinmastodon.web.auth.BearerTokenAuthenticationFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Security configuration for integration tests.
 * 
 * This configuration enforces proper OAuth token validation for protected endpoints
 * while allowing public endpoints to be accessed without authentication.
 * 
 * Authentication rules match Mastodon API behavior:
 * - Public endpoints (GET instance info, public timelines) - no auth required
 * - User endpoints (accounts, statuses, timelines) - auth required
 * - Admin endpoints - auth required with admin scope
 */
@TestConfiguration
@EnableWebSecurity
@Import({RequestIdFilter.class})
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(
            HttpSecurity http,
            OAuthAccessTokenService accessTokenService,
            UserRepository userRepository) throws Exception {
        http
            // Disable CSRF for tests
            .csrf(csrf -> csrf.disable())
            
            // Disable frame options (allows H2 console to work)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            )
            
            // Return 401 instead of 403 for unauthenticated requests
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            
            // Add bearer token authentication filter
            .addFilterBefore(
                new BearerTokenAuthenticationFilter(accessTokenService, userRepository),
                UsernamePasswordAuthenticationFilter.class
            )
            
            // Authorization rules matching Mastodon API behavior
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(HttpMethod.GET, "/api/v1/instance").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/instance/peers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/instance/activity").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/instance/extended_description").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/instance/rules").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v2/instance").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/timelines/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/timelines/tag/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/directory").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/custom_emojis").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/announcements").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/statuses/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/statuses/*/context").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/statuses/*/favourited_by").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/statuses/*/reblogged_by").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/*/statuses").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/*/followers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/*/following").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/accounts/*/featured_tags").permitAll()
                
                // Media and polls - require authentication
                 // .requestMatchers(HttpMethod.GET, "/api/v1/media/*").permitAll()
                 // .requestMatchers(HttpMethod.GET, "/api/v2/media/*").permitAll()
                 // .requestMatchers(HttpMethod.GET, "/api/v1/polls/*").permitAll()
                
                // OAuth endpoints - no authentication required for token exchange
                .requestMatchers(HttpMethod.POST, "/oauth/token").permitAll()
                .requestMatchers(HttpMethod.POST, "/oauth/revoke").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/apps").permitAll()
                
                // Health and actuator endpoints
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/error").permitAll()
                
                // WebFinger - public
                .requestMatchers(HttpMethod.GET, "/.well-known/webfinger").permitAll()
                
                // Admin endpoints require authentication and admin scope
                // Accept either 'admin' scope or 'admin:read'/'admin:write' scopes
                .requestMatchers("/api/v1/admin/**").hasAnyAuthority("SCOPE_admin", "SCOPE_admin:read", "SCOPE_admin:write")
                
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                
                // Allow everything else
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
