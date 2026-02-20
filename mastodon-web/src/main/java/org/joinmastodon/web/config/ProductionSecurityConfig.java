package org.joinmastodon.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Production security configuration.
 * 
 * This configuration provides proper security headers and CSRF protection
 * for production deployments.
 * 
 * Security features:
 * - Content Security Policy (CSP)
 * - X-Frame-Options
 * - X-XSS-Protection
 * - X-Content-Type-Options
 * - Referrer-Policy
 * - CORS configuration
 * - CSRF protection for session-based auth
 */
@Configuration
@EnableWebSecurity
@Profile("prod")
public class ProductionSecurityConfig {

    @Bean
    public SecurityFilterChain productionSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Security headers
            .headers(headers -> headers
                // Prevent clickjacking
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                
                // XSS protection
                .xssProtection(xss -> xss.disable())
                .addHeaderWriter((request, response) -> {
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                })
                
                // Content type sniffing protection
                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())
                .addHeaderWriter((request, response) -> {
                    response.setHeader("X-Content-Type-Options", "nosniff");
                })
                
                // Content Security Policy
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "media-src 'self' https:; " +
                        "connect-src 'self' wss: https:; " +
                        "frame-ancestors 'self'; " +
                        "form-action 'self';"
                    )
                )
                
                // Referrer Policy
                .addHeaderWriter((request, response) -> {
                    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                })
                
                // Permissions Policy
                .addHeaderWriter((request, response) -> {
                    response.setHeader("Permissions-Policy", 
                        "geolocation=(), microphone=(), camera=()");
                })
            )
            
            // CSRF protection for session-based auth
            // Note: For API-only with token auth, CSRF is less critical
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
                .ignoringRequestMatchers("/oauth/**")
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/instance").permitAll()
                .requestMatchers("/api/v1/instance/**").permitAll()
                .requestMatchers("/api/v1/timelines/public").permitAll()
                .requestMatchers("/.well-known/**").permitAll()
                .requestMatchers("/users/**").permitAll()
                .requestMatchers("/inbox").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // OAuth endpoints
                .requestMatchers("/oauth/**").permitAll()
                .requestMatchers("/api/v1/apps").permitAll()
                
                // Static resources
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                
                // Allow all other requests (will be handled by filters)
                .anyRequest().permitAll()
            );
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins - should be configured via environment variable
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Allowed methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Idempotency-Key"
        ));
        
        // Exposed headers
        configuration.setExposedHeaders(Arrays.asList(
            "Link",
            "X-RateLimit-Limit",
            "X-RateLimit-Remaining",
            "X-RateLimit-Reset",
            "X-Request-Id"
        ));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Cache preflight response
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/oauth/**", configuration);
        
        return source;
    }
}
