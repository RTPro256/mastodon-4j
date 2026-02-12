package org.joinmastodon.web.config;

import java.time.Duration;
import org.joinmastodon.core.service.OAuthAccessTokenService;
import org.joinmastodon.core.service.RateLimitService;
import org.joinmastodon.web.api.ApiVersion;
import org.joinmastodon.web.auth.BearerTokenAuthenticationFilter;
import org.joinmastodon.web.auth.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuthAccessTokenService accessTokenService,
            RateLimitService rateLimitService,
            @Value("${mastodon.rate-limit.max-requests:300}") int maxRequests,
            @Value("${mastodon.rate-limit.window-seconds:60}") long windowSeconds) throws Exception {
        BearerTokenAuthenticationFilter bearerFilter = new BearerTokenAuthenticationFilter(accessTokenService);
        RateLimitFilter rateLimitFilter = new RateLimitFilter(rateLimitService, maxRequests, Duration.ofSeconds(windowSeconds));

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/oauth/token", "/oauth/revoke").permitAll()
                        .requestMatchers(HttpMethod.POST, ApiVersion.V1 + "/apps").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiVersion.V1 + "/instance/**").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiVersion.V1 + "/statuses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiVersion.V1 + "/media/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/media/**").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiVersion.V1 + "/polls/**").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiVersion.V1 + "/accounts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiVersion.V1 + "/timelines/public").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiVersion.V1 + "/timelines/tag/**").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiVersion.V2 + "/search").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(bearerFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}
